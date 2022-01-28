package by.gdev.http.upload.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * TODO improve description for params
 * Converted json file content of the file to java object
 */

public interface GsonService {
	/**
	 * @param <T> type return object
	 * @param url - url address 
	 * @param class1 java object
	 * @param cache If cache true file exists and hashsum is valid it should
	 *              return content without head request. 
	 *              If cache false we need to do http head request to check version in the cache with ETag
	 * @return T 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	<T> T getObject(String url, Class<T> class1, boolean cache) throws FileNotFoundException, IOException, NoSuchAlgorithmException;
	
	/**
	 * 
	 * @param <T>
	 * @param urls
	 * @param urn
	 * @param class1
	 * @param cache
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	
	<T> T getObjectByUrls(List<String> urls, String urn, Class<T> class1, boolean cache) throws FileNotFoundException, IOException, NoSuchAlgorithmException;
	
}