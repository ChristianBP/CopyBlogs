package cbp.copyblogs.Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONObject;

import cbp.copyblogs.BlogCopier;
import cbp.copyblogs.DestinationBlog;
import cbp.copyblogs.SourceBlog;
import cbp.copyblogs.WordpressDbConnection;
import cbp.copyblogs.copyfiles.CopyFiles;
import cbp.copyblogs.copyfiles.CopyFilesL2L;
import cbp.copyblogs.copyfiles.CopyFilesR2R;

/**
 * 
 * @author Christian Parker
 *
 */
public class Main {
	
	public static void main(String[] args) throws Exception {
		JSONObject config = fetchConfiguration(BlogCopier.getConfigLocation(), "config.json");
		
		SourceBlog sourceBlog = new SourceBlog(	config.getString("sourceDbHost"), 
												config.getString("pathToBeCopied"), 
												config.getString("sourceDbUrl"), 
												config.getString("sourceDbDriver"), 
												config.getString("sourceTablePrefix")
											);
		
		WordpressDbConnection sourceSchema = new WordpressDbConnection(	config.getString("sourceDbHost"), 
																		config.getString("pathToBeCopied"), 
																		config.getString("sourceDbSchemaUrl"), 
																		config.getString("sourceDbDriver"), 
																		config.getString("sourceTablePrefix")
																	);
		
		DestinationBlog destinationBlog = new DestinationBlog(	config.getString("destinationDbHost"), 
																config.getString("pathToBeCopied"), 
																config.getString("destinationDbUrl"), 
																config.getString("destinationDbDriver"), 
																config.getString("destinationTablePrefix")
															);

		BlogCopier copyBlog = new BlogCopier(sourceBlog, destinationBlog, sourceSchema);
		copyBlog.copy();
		
		
		JSONObject fileServerConfig = fetchConfiguration(BlogCopier.getConfigLocation(), "fileServerConfig.json");
		CopyFiles copyFiles;
		
		if(config.getBoolean("remoteCopy")) {
			copyFiles = new CopyFilesR2R(	fileServerConfig.getString("sourceAdditionalFilesPath"), 
											fileServerConfig.getString("sourceFileServerUsername"), 
											fileServerConfig.getString("sourceFileServer"), 
											fileServerConfig.getString("intermediaryAdditionalFilesPath"), 
											fileServerConfig.getString("destinationAdditionalFilesPath"), 
											fileServerConfig.getString("destinationFileServerUsername"), 
											fileServerConfig.getString("destinationFileServer"), 
											copyBlog.getSourceBlogId(), 
											copyBlog.getDestinationBlogId());
		}
		else {
			copyFiles = new CopyFilesL2L(	fileServerConfig.getString("sourceAdditionalFilesPath"), 
											fileServerConfig.getString("destinationAdditionalFilesPath"), 
											copyBlog.getSourceBlogId(), 
											copyBlog.getDestinationBlogId());
		}
		
		copyFiles.copy();
	}

	
    /**
     * Get a JSON config
     * 
     * @param configLocation
     * @param configName
     * @return JSONObject to be queried for variables
     */
    public static JSONObject fetchConfiguration(String configLocation, String configName) {
        BufferedReader br = null;
        try {
            String configText = "";
            br = new BufferedReader(new FileReader(configLocation + configName));
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                configText += currentLine;
            }
            JSONObject config = new JSONObject(configText);
            return config;

        } 
        catch (FileNotFoundException ex) {} 
        catch (IOException ex) {} 
        finally {
			try {
			    if (br != null) {
			        br.close();
			    }
			} 
			catch (IOException ex) {}
        }
        return null;

    }
}
