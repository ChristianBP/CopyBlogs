package cbp.copyblogs;

import cbp.copyblogs.dao.ApplicationDao;

/**
 * 
 * @author Christian Parker
 *
 * Has some functions that are specific to the source blog.
 */
public class SourceBlog extends WordpressDbConnection {	
	
	public SourceBlog(String hostName, String path, String dbUrl, String dbDriver, String tablePrefix) {
		super(hostName, path, dbUrl, dbDriver, tablePrefix);
	}

	
	
    /**
     * Based on the path, get the blog id that we want to copy.
     * 
     * @param sourceApplicationDb the Handle for the sourceDb with the ApplicationDao class attached
     * @return Blog Id to be copied
     */
	public Integer getSourceBlogId(ApplicationDao sourceApplicationDb) {
		return sourceApplicationDb.getBlogId(tablePrefix + "blogs", path);
	}



	// GETTERS
	public String getPath() {
		return path;
	}
}
