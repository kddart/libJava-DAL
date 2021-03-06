/*
 * dalclient library - provides utilities to assist in using KDDart-DAL servers
 * Copyright (C) 2015,2016,2017  Diversity Arrays Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diversityarrays.dalclient.httpandroid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections15.Factory;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
import ch.boye.httpclientandroidlib.entity.mime.content.InputStreamBody;
import ch.boye.httpclientandroidlib.impl.client.BasicResponseHandler;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.impl.client.HttpClients;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.diversityarrays.dalclient.DalUtil;
import com.diversityarrays.dalclient.HttpResponseInfo;
import com.diversityarrays.dalclient.http.DalCloseableHttpClient;
import com.diversityarrays.dalclient.http.DalCloseableHttpResponse;
import com.diversityarrays.dalclient.http.DalHttpFactory;
import com.diversityarrays.dalclient.http.DalRequest;
import com.diversityarrays.dalclient.http.DalResponseHandler;
import com.diversityarrays.dalclient.util.Pair;


public class AndroidDalHttpFactory implements DalHttpFactory {

	@Override
	public DalRequest createHttpGet(String url) {
		return new AndroidDalRequest(new HttpGet(url));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DalResponseHandler<String> createBasicResponseHandler() {
		return new AndroidDalResponseHandler(new BasicResponseHandler());
	}

	@Override
	public DalCloseableHttpClient createCloseableHttpClient(SSLContext context) {
		HttpClientBuilder builder = HttpClients.custom();
		builder.setSslcontext(context);
		return new AndroidDalCloseableHttpClient(builder.build());
	}

	@Override
	public DalRequest createHttpPost(String url,
			List<Pair<String,String>> pairs, Charset charset) {
		
		List<NameValuePair> collectedPairs = new ArrayList<NameValuePair>(pairs.size());
		for (Pair<String,String> p : pairs) {
			collectedPairs.add(new BasicNameValuePair(p.a, p.b));
		}
		
		
		HttpPost post = new HttpPost(url);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(collectedPairs, charset);
		post.setEntity(entity);
		

		return new AndroidDalRequest(post);
	}
	

	@Override
	public DalRequest createForUpload(String url, List<Pair<String,String>> pairs, String rand_num, String namesInOrder, String signature, File fileForUpload) {

		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		for (Pair<String,String> pair : pairs) {
			builder.addTextBody(pair.a, pair.b);
		}
		
		builder.addPart("uploadfile", new FileBody(fileForUpload))
				.addTextBody("rand_num", rand_num)
				.addTextBody("url", url);

		
		HttpEntity entity = builder
				.addTextBody("param_order", namesInOrder)
				.addTextBody("signature", signature)
				.build();

		HttpPost post = new HttpPost(url);
		post.setEntity(entity);

		return new AndroidDalRequest(post);
	}


	@Override
	public DalRequest createForUpload(String url, List<Pair<String,String>> pairs,
			String rand_num, String namesInOrder, String signature,
			Factory<InputStream> factory) 
	{
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
				.addPart("uploadfile", new InputStreamBody(factory.create(), "uploadfile"))
				.addTextBody("rand_num", rand_num)
				.addTextBody("url", url);
		
		for (Pair<String,String> pair : pairs) {
			builder.addTextBody(pair.a, pair.b);
		}
		
		HttpEntity entity = builder
				.addTextBody("param_order", namesInOrder)
				.addTextBody("signature", signature)
				.build();

		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		return new AndroidDalRequest(post);
	}


	@Override
	public DalResponseHandler<HttpResponseInfo> createResponseHandler() {
		
		return new DalResponseHandler<HttpResponseInfo>() {
			@Override
			public HttpResponseInfo handleResponse(DalCloseableHttpResponse response) throws IOException {

				HttpResponseInfo result = new HttpResponseInfo();

				result.headers = response.getAllHeaders();
				result.httpStatusCode = response.getStatusCode();

				if (DalUtil.isHttpStatusCodeOk(result.httpStatusCode)) {
					// All is well with the world!
				}
				else {
					result.httpErrorReason = response.getReasonPhrase();
				}

				result.serverResponse = response.getEntityAsString();

				return result;
			}
		};
		
	}

}
