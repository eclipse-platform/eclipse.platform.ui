<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" indent="yes"/>
 
   <xsl:param name="eclipse_base_url" select="''"/>

   <xsl:template match="/infoset">
      <ul class="view">
      <xsl:apply-templates/>
      </ul>
   </xsl:template>


   <xsl:template match="infoview">
   <li><xsl:value-of select="@label"/>
       <ul class="expanded">
       <xsl:apply-templates/>
       </ul>
   </li>
   </xsl:template>


   <xsl:template match="topic">
   <li>

       <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="*">node</xsl:when>
            <xsl:otherwise>leaf</xsl:otherwise>
          </xsl:choose>
       </xsl:attribute>

       <xsl:element name="a">
          <xsl:attribute name="href">
             <xsl:choose>
                <xsl:when test="@href!=''"><xsl:value-of select="$eclipse_base_url"/>/help<xsl:value-of select="@href"/></xsl:when>
                <xsl:otherwise>javascript:void 0</xsl:otherwise>
             </xsl:choose>
          </xsl:attribute>
          <xsl:value-of select="@label"/>
       </xsl:element>
       <xsl:if test="*">
          <ul class="collapsed">
          <xsl:apply-templates/>
          </ul>
       </xsl:if>
   </li>
   </xsl:template>


</xsl:stylesheet>