/*
 * Copyright 2008 Oakland Software Incorporated and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *     IBM Corporation - enabling JNI calls for gconfInit method (bug 232495)	
 */

#include <jni.h>

#include <glib.h>
#include <glib/gslist.h>
#include <gconf/gconf-value.h>
#include <gconf/gconf-client.h>

#ifdef __linux__
#include <string.h>
#else
#include <strings.h>
#endif

#include "gnomeproxy.h"

static GConfClient *client= NULL;

static jclass proxyInfoClass;
static jclass stringClass;
static jmethodID proxyInfoConstructor;
static jmethodID toString;

static jmethodID hostMethod;
static jmethodID portMethod;
static jmethodID userMethod;
static jmethodID passwordMethod;

#define CHECK_NULL(X) { if ((X) == NULL) fprintf (stderr,"JNI error at line %d\n", __LINE__); } 

/*
 * Class:     org_eclipse_core_internal_net_proxy_unix_UnixProxyProvider
 * Method:    gconfInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_eclipse_core_internal_net_proxy_unix_UnixProxyProvider_gconfInit(
		JNIEnv *env, jclass clazz) {

	g_type_init();
	client = gconf_client_get_default();
	jclass cls= NULL;
	CHECK_NULL(cls = (*env)->FindClass(env, "org/eclipse/core/internal/net/ProxyData"));
	proxyInfoClass = (*env)->NewGlobalRef(env, cls);

	CHECK_NULL(cls = (*env)->FindClass(env, "java/lang/String"));
	stringClass = (*env)->NewGlobalRef(env, cls);

	CHECK_NULL(proxyInfoConstructor = (*env)->GetMethodID(env, proxyInfoClass, "<init>", "(Ljava/lang/String;)V"));

	CHECK_NULL(toString = (*env)->GetMethodID(env, proxyInfoClass, "toString", "()Ljava/lang/String;"));

	CHECK_NULL(hostMethod = (*env)->GetMethodID(env, proxyInfoClass, "setHost",
					"(Ljava/lang/String;)V"));
	CHECK_NULL(portMethod = (*env)->GetMethodID(env, proxyInfoClass, "setPort",
					"(I)V"));
	CHECK_NULL(userMethod = (*env)->GetMethodID(env, proxyInfoClass, "setUserid",
					"(Ljava/lang/String;)V"));
	CHECK_NULL(passwordMethod = (*env)->GetMethodID(env, proxyInfoClass, "setPassword",
					"(Ljava/lang/String;)V"));
}

/*
 * Class:     org_eclipse_core_internal_net_UnixProxyProvider
 * Method:    getGConfProxyInfo
 * Signature: ([Ljava/lang/String);
 */
JNIEXPORT jobject JNICALL Java_org_eclipse_core_internal_net_proxy_unix_UnixProxyProvider_getGConfProxyInfo(
		JNIEnv *env, jclass clazz, jstring protocol) {

	jboolean isCopy;
	const char *cprotocol;

	jobject proxyInfo= NULL;

	if (client == NULL) {
		Java_org_eclipse_core_internal_net_proxy_unix_UnixProxyProvider_gconfInit(env, clazz);
	}

	CHECK_NULL(proxyInfo = (*env)->NewObject(env, proxyInfoClass, proxyInfoConstructor, protocol));

	cprotocol = (*env)->GetStringUTFChars(env, protocol, &isCopy);
	if (cprotocol == NULL)
		return NULL;

	//printf("cprotocol: %s\n", cprotocol);

	// use_same_proxy means we use the http value for everything
	gboolean useSame = gconf_client_get_bool(client,
			"/system/http_proxy/use_same_proxy", NULL);

	if (strcasecmp(cprotocol, "http") == 0 || useSame) {
		gboolean useProxy = gconf_client_get_bool(client,
				"/system/http_proxy/use_http_proxy", NULL);
		if (!useProxy) {
			proxyInfo = NULL;
			goto exit;
		}

		gchar *host = gconf_client_get_string(client,
				"/system/http_proxy/host", NULL);
		jobject jhost = (*env)->NewStringUTF(env, host);
		(*env)->CallVoidMethod(env, proxyInfo, hostMethod, jhost);

		gint port = gconf_client_get_int(client, "/system/http_proxy/port",
				NULL);
		(*env)->CallVoidMethod(env, proxyInfo, portMethod, port);

		gboolean reqAuth = gconf_client_get_bool(client,
				"/system/http_proxy/use_authentication", NULL);
		if (reqAuth) {

			gchar *user = gconf_client_get_string(client,
					"/system/http_proxy/authentication_user", NULL);
			jobject juser = (*env)->NewStringUTF(env, user);
			(*env)->CallVoidMethod(env, proxyInfo, userMethod, juser);

			gchar *password = gconf_client_get_string(client,
					"/system/http_proxy/authentication_password", NULL);
			jobject jpassword = (*env)->NewStringUTF(env, password);
			(*env)->CallVoidMethod(env, proxyInfo, passwordMethod,
					jpassword);
		}
		goto exit;
	}

	// Everything else applies only if the system proxy mode is manual
	gchar *mode = gconf_client_get_string(client, "/system/proxy/mode", NULL);
	if (strcasecmp(mode, "manual") != 0) {
		proxyInfo = NULL;
		goto exit;
	}

	char selector[100];

	if (strcasecmp(cprotocol, "https") == 0) {
		strcpy(selector, "/system/proxy/secure_");
	} else if (strcasecmp(cprotocol, "socks") == 0) {
		strcpy(selector, "/system/proxy/socks_");
	} else if (strcasecmp(cprotocol, "ftp") == 0) {
		strcpy(selector, "/system/proxy/ftp_");
	} else {
		proxyInfo = NULL;
		goto exit;
	}

	char useSelector[100];
	strcpy(useSelector, selector);

	gchar *host = gconf_client_get_string(client, strcat(useSelector, "host"),
			NULL);
	jobject jhost = (*env)->NewStringUTF(env, host);
	(*env)->CallVoidMethod(env, proxyInfo, hostMethod, jhost);

	strcpy(useSelector, selector);
	gint port = gconf_client_get_int(client, strcat(useSelector, "port"), NULL);
	(*env)->CallVoidMethod(env, proxyInfo, portMethod, port);

	exit: if (isCopy == JNI_TRUE)
		(*env)->ReleaseStringUTFChars(env, protocol, cprotocol);
	return proxyInfo;
}

typedef struct {
	jobjectArray npHostArray;
	JNIEnv *env;
	int index;
} ListProcContext;

// user_data is the ListProcContext
void listProc(gpointer data, gpointer user_data) {
	ListProcContext *lpc = user_data;
	jobject jnpHost = (*lpc->env)->NewStringUTF(lpc->env, (char *)data);
	(*lpc->env)->SetObjectArrayElement(lpc->env, lpc->npHostArray,
			lpc->index++, jnpHost);
}

/*
 * Class:     org_eclipse_core_internal_net_UnixProxyProvider
 * Method:    getGConfNonProxyHosts
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_org_eclipse_core_internal_net_proxy_unix_UnixProxyProvider_getGConfNonProxyHosts(
		JNIEnv *env, jclass clazz) {

	if (client == NULL) {
		Java_org_eclipse_core_internal_net_proxy_unix_UnixProxyProvider_gconfInit(env, clazz);
	}

	GSList *npHosts;
	int size;

	npHosts = gconf_client_get_list(client, "/system/http_proxy/ignore_hosts",
			GCONF_VALUE_STRING, NULL);
	size = g_slist_length(npHosts);

	// TODO - I'm not sure this is really valid, it's from the JVM implementation
	// of ProxySelector
	if (size == 0) {
		npHosts = gconf_client_get_list(client, "/system/proxy/no_proxy_for",
				GCONF_VALUE_STRING, NULL);
	}
	size = g_slist_length(npHosts);

	jobjectArray ret = (*env)->NewObjectArray(env, size, stringClass, NULL);

	ListProcContext lpc;
	lpc.env = env;
	lpc.npHostArray = ret;
	lpc.index = 0;

	g_slist_foreach(npHosts, listProc, &lpc);
	return ret;
}

