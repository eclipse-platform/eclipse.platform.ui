package org.eclipse.jface.text.formatter;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.util.Assert;


/**
 * Standard implementation of <code>IContentFormatter</code>.
 * The formatter determines the partitioning of the document region to be
 * formatted. For each partition it determines all document positions 
 * which are affected when text changes are applied to the partition.
 * Those which overlap with the partition are remembered as character 
 * positions. These character positions are passed over to the formatting strategy
 * registered for the partition's content type. The formatting strategy returns
 * a string containing the formatted document partition as well as the adapted 
 * character positions. The formatted partition replaces the old content of the
 * partition. The remembered document postions are updated with the adapted 
 * character positions. In addition, all document positions which either embrace the
 * formatted partition or refer to document ranges below the partition are adapted 
 * according to the change of the length of the partition.<p>
 * Usually, clients instantiate this class and configure it before using it.
 *
 * @see IContentFormatter
 * @see IDocument
 * @see ITypedRegion
 * @see Position
 */
public class ContentFormatter implements IContentFormatter {
		
	/**
	 * Defines a reference to either the offset or the end offset of
	 * a particular position.
	 */
	static class PositionReference {
		
		/** The referenced position */
		protected Position fPosition;
		/** The reference to either the offset or the end offset */
		protected boolean fRefersToOffset;
		
		protected PositionReference(Position position, boolean refersToOffset) {
			fPosition= position;
			fRefersToOffset= refersToOffset;
		}
		
		/**
		 * Returns the offset of the referenced position.
		 */
		protected int getOffset() {
			return fPosition.getOffset();
		}
		
		/**
		 * Manipulates the offset of the referenced position.
		 */
		protected void setOffset(int offset) {
			fPosition.setOffset(offset);
		}
		
		/**
		 * Returns the length of the referenced position.
		 */
		protected int getLength() {
			return fPosition.getLength();
		}
		
		/**
		 * Manipulates the length of the referenced position.
		 */
		protected void setLength(int length) {
			fPosition.setLength(length);
		}
		
		/**
		 * Returns whether this reference points to the offset or endoffset
		 * of the references position.
		 */
		protected boolean referesToOffset() {
			return fRefersToOffset;
		}
	};
	
	/**
	 * The position updater used to adapt all those position which refer
	 * to document ranges which completely embrace or are below the formatted
	 * partition. It is also used to update the partitions.
	 *
	 * @see IPositionUpdater
	 * @see DefaultPositionUpdater
	 */
	class NonDeletingPositionUpdater extends DefaultPositionUpdater {
		
		protected NonDeletingPositionUpdater(String category) {
			super(category);
		}
		
		/*
		 * @see DefaultPositionUpdater#notDeleted()
		 */
		protected boolean notDeleted() {
			return true;
		}
	};
	
	
	/** Internal position category used for the formatter partitioning */
	private final static String PARTITIONING= "__formatter_partitioning";
	/** Internal position category used for positions to be updated */
	private final static String UPDATING= "__formatter_updating";
	
	/** The map of <code>IFormatterStrategy</code> objects */
	private Map fStrategies;
	
	/** The partition information managing document position categories */
	private String[] fPartitionManagingCategories;
	/** The document's remembered position updaters */
	private IPositionUpdater[] fOriginalUpdaters;
	/** The list of references to offset and end offset of all overlapping positions */
	private List fOverlappingPositionReferences;
	/** Position updater used for partitioning positions */
	private IPositionUpdater fPartitioningUpdater;	
	/** Position updater used for embracing and follow up positions */
	private IPositionUpdater fPositionUpdater;	
	
	
	
	/**
	 * Creates a new content formatter.
	 */
	public ContentFormatter() {
	}
	/**
	 * Reinstalls all of the original position updaters of the document and
	 * also removes all of the formatter internal updaters and categories.
	 *
	 * @param document the document that has been formatted
	 */
	private void addPositionUpdaters(IDocument document) {
		
		try {
			
			document.removePositionUpdater(fPositionUpdater);
			fPositionUpdater= null;
			
			document.removePositionUpdater(fPartitioningUpdater);
			document.removePositionCategory(PARTITIONING);
			fPartitioningUpdater= null;
			
		} catch (BadPositionCategoryException x) {
			// should not happen
		}
		
		if (fOriginalUpdaters != null) {
			
			for (int i= 0; i < fOriginalUpdaters.length; i++)
				document.addPositionUpdater(fOriginalUpdaters[i]);
			
			fOriginalUpdaters= null;
		}
	}
	/**
	 * Determines all embracing, overlapping, and follow up positions 
	 * for the given region of the document.
	 *
	 * @param document the document to be formatted
	 * @param offset the offset of the document region to be formatted
	 * @param length the length of the document to be formatted
	 */
	private void determinePositionsToUpdate(IDocument document, int offset, int length) {
		
		String[] categories= document.getPositionCategories();
		if (categories != null) {
			for (int i= 0; i < categories.length; i++) {
				
				if (ignoreCategory(categories[i]))
					continue;
					
				try {
					
					Position[] positions= document.getPositions(categories[i]);
					
					for (int j= 0; j < positions.length; j++) {
						
						Position p= (Position) positions[j];
						if (p.overlapsWith(offset, length)) {
							
							if (offset <= p.getOffset())
								fOverlappingPositionReferences.add(new PositionReference(p, true));
							
							if (p.getOffset() + p.getLength() <= offset + length) {
								fOverlappingPositionReferences.add(new PositionReference(p, false));
							} else {
								try {
									document.addPosition(UPDATING, p);
								} catch (BadLocationException x) {
								}
							}
							
						} else if (offset + length <= p.getOffset()) {
							try {
								document.addPosition(UPDATING, p);
							} catch (BadLocationException x) {
							}
						}
					}
				} catch (BadPositionCategoryException x) {
				}
			}
		}
	}
	/**
	 * Formats one partition after the other using the formatter strategy registered for
	 * the partition's content type.
	 *
	 * @param document to document to be formatted
	 * @param ranges the partitioning of the document region to be formatted
	 */
	private void format(IDocument document, TypedPosition[] ranges) {
		
		for (int i= 0; i < ranges.length; i++) {
			
			int offset= ranges[i].getOffset();
			int length= ranges[i].getLength();
			
			IFormattingStrategy s= getFormattingStrategy(ranges[i].getType());
			if (s != null) {
				try {
					
					document.addPositionCategory(UPDATING);
					
					String content= document.get(offset, length);
					int[] positions= getAffectedPositions(document, offset, length);
					String formatted= s.format(content, isLineStart(document, offset), getIndentation(document, offset), positions);
					document.replace(offset, length, formatted);
					updateAffectedPositions(document, positions, offset);
					
					document.removePositionCategory(UPDATING);
					
				} catch (BadLocationException x) {
					// should not happen
				} catch (BadPositionCategoryException x) {
					// may not happen
				}
			}
		}
	}
	/*
	 * @see IContentFormatter#format
	 */
	public void format(IDocument document, IRegion region) {
		
		removePositionUpdaters(document);
		
		try {
			
			TypedPosition[] ranges= getPartitioning(document, region);
			if (ranges != null) {
				start(ranges, getIndentation(document, region.getOffset()));
				format(document, ranges);
				stop(ranges);
			}
			
		} catch (BadLocationException x) {
		}
			
		addPositionUpdaters(document);
	}
	/**
	 * Returns all offset and the end offset of all positions overlapping with the 
	 * specified document range.
	 *
	 * @param document the document to be formatted
	 * @param offset the offset of the document region to be formatted
	 * @param length the length of the document to be formatted
	 * @return all character positions of the interleaving positions
	 */
	private int[] getAffectedPositions(IDocument document, int offset, int length) {
		
		fOverlappingPositionReferences= new ArrayList();
		
		determinePositionsToUpdate(document, offset, length);
		
		int[] positions= new int[fOverlappingPositionReferences.size()];
		for (int i= 0; i < positions.length; i++) {
			PositionReference r= (PositionReference) fOverlappingPositionReferences.get(i);
			positions[i]= (r.referesToOffset() ? r.getOffset() : r.getOffset() + r.getLength()) - offset;
		}
		
		return positions;
	}
	/*
	 * @see IContentFormatter#getFormattingStrategy
	 */
	public IFormattingStrategy getFormattingStrategy(String contentType) {
		
		Assert.isNotNull(contentType);
		
		if (fStrategies == null)
			return null;
								
		return (IFormattingStrategy) fStrategies.get(contentType);
	}
	/**
	 * Returns the indentation of the line of the given offset.
	 *
	 * @param document the document
	 * @param offset the offset
	 * @return the indentation of the line of the offset
	 * @exception BadLocationException if offset is invalid in document
	 */
	private String getIndentation(IDocument document, int offset) throws BadLocationException {
		
		int start= document.getLineOfOffset(offset);
		start= document.getLineOffset(start);
		
		int end= start;
		char c= document.getChar(end);
		while ('\t' == c || ' ' == c)
			c= document.getChar(++end);
			
		return document.get(start, end - start);
	}
	/**
	 * Returns the partitioning of the given region of the specified document.
	 * As one partition after the other will be formatted and formatting will 
	 * probably change the length of the formatted partition, it must be kept 
	 * track of the modifications in order to submit the correct partition to all 
	 * formatting strategies. For this, all partitions are remembered as positions
	 * in a dedicated position category. (As formatting stratgies might rely on each
	 * other, calling them in reversed order is not an option.)
	 *
	 * @param document the document
	 * @param region the region for which the partitioning must be determined
	 * @return the partitioning of the specified region
	 * @exception BadLocationException of region is invalid in the document
	 */
	private TypedPosition[] getPartitioning(IDocument document, IRegion region) throws BadLocationException {
		ITypedRegion[] regions= document.computePartitioning(region.getOffset(), region.getLength());
		TypedPosition[] positions= new TypedPosition[regions.length];
		
		for (int i= 0; i < regions.length; i++) {
			positions[i]= new TypedPosition(regions[i]);
			try {
				document.addPosition(PARTITIONING, positions[i]);
			} catch (BadPositionCategoryException x) {
				// should not happen
			}
		}
		
		return positions;
	}
	/**
	 * Determines whether the given document position category should be ignored
	 * by this formatter's position updating.
	 *
	 * @param category the category to check
	 * @return <code>true</code> if the category should be ignored, <code>false</code> otherwise
	 */
	private boolean ignoreCategory(String category) {
		
		if (PARTITIONING.equals(category))
			return true;
			
		if (UPDATING.equals(category))
			return true;
			
		if (fPartitionManagingCategories != null) {
			for (int i= 0; i < fPartitionManagingCategories.length; i++) {
				if (fPartitionManagingCategories[i].equals(category))
					return true;
			}
		}
		
		return false;
	}
	/**
	 * Determines whether the offset is the beginning of a line in the given document.
	 *
	 * @param document the document
	 * @param offset the offset
	 * @return <code>true</code> if offset is the beginning of a line
	 * @exception BadLocationException if offset is invalid in document
	 */
	private boolean isLineStart(IDocument document, int offset) throws BadLocationException {
		int start= document.getLineOfOffset(offset);
		start= document.getLineOffset(start);
		return (start == offset);
	}
	/**
	 * Removes all of the original positions updaters from the document, remembers them,
	 * and installs those which the formatter needs to keep track of the partitions, the
	 * embracing and follow up positions.
	 *
	 * @param document the document to be formatted
	 */
	private void removePositionUpdaters(IDocument document) {
		
		fOriginalUpdaters= document.getPositionUpdaters();
		if (fOriginalUpdaters != null) {
			for (int i= 0; i < fOriginalUpdaters.length; i++)
				document.removePositionUpdater(fOriginalUpdaters[i]);
		}
		
		fPartitioningUpdater= new NonDeletingPositionUpdater(PARTITIONING);
		document.addPositionCategory(PARTITIONING);
		document.addPositionUpdater(fPartitioningUpdater);
		
		fPositionUpdater= new NonDeletingPositionUpdater(UPDATING);
		document.addPositionUpdater(fPositionUpdater);
	}
	/**
	 * Registers a strategy for a particular content type. If there is already a strategy
	 * registered for this type, the new strategy is registered instead of the old one.
	 *
	 * @param strategy the formatting strategy to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register, may not be <code>null</code>
	 */
	public void setFormattingStrategy(IFormattingStrategy strategy, String contentType) {
		
		Assert.isNotNull(contentType);
			
		if (fStrategies == null)
			fStrategies= new HashMap();
			
		if (strategy == null)
			fStrategies.remove(contentType);
		else
			fStrategies.put(contentType, strategy);
	}
	/**
	 * Informs this content formatter about the names of those position categories
	 * which are used to manage the document's partitioning information and thus should
	 * be ignored when this formatter updates positions.
	 *
	 * @param categories the categories to be ignored
	 */
	public void setPartitionManagingPositionCategories(String[] categories) {
		fPartitionManagingCategories= categories;
	}
	/**
	 * Fires <code>formatterStarts</code> to all formatter strategies which will be
	 * involved in the forthcoming formatting process.
	 * 
	 * @param ranges the partitioning of the region to be formatted
	 * @param indentation the initial indentation
	 */
	private void start(TypedPosition[] ranges, String indentation) {
		for (int i= 0; i < ranges.length; i++) {
			IFormattingStrategy s= getFormattingStrategy(ranges[i].getType());
			if (s != null)
				s.formatterStarts(indentation);
		}
	}
	/**
	 * Fires <code>formatterStops</code> to all formatter strategies which were
	 * involved in the formatting process which is about to terminate.
	 *
	 * @param ranges the partitioning of the document region which has been formatted
	 */
	private void stop(TypedPosition[] ranges) {
		for (int i= 0; i < ranges.length; i++) {
			IFormattingStrategy s= getFormattingStrategy(ranges[i].getType());
			if (s != null)
				s.formatterStops();
		}
	}
	/**
	 * Updates all the overlapping positions. Note, the embracing and follow up positions are
	 * automatically updated by the installed document position updaters.
	 *
	 * @param document the document to has been formatted
	 * @param positions the adapted character positions to be used to update the document positions
	 * @param offset the offset of the document region that has been formatted
	 */
	private void updateAffectedPositions(IDocument document, int[] positions, int offset) {
		for (int i= 0; i < positions.length; i++) {
			PositionReference r= (PositionReference) fOverlappingPositionReferences.get(i);
			if (r.referesToOffset())
				r.setOffset(offset + positions[i]);
			else
				r.setLength((offset + positions[i]) - r.getOffset());
		}
		
		fOverlappingPositionReferences= null;
	}
}
