package cbp.copyblogs.copyfiles;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


/**
 * 
 * @author Christian Parker
 *
 * Class for copying files from one remote folder to another
 */
public class CopyFilesR2R extends CopyFiles{
	
	private static final String identityLocation = "~/.ssh/keys/copyBlogs";
	private static final String knownHostsLocation = "~/.ssh/known_hosts";
    
    private final String sourceAdditionalFilesPath;
    private final String sourceFileServerUsername;
    private final String sourceFileServer;
    private final int sourceFileServerPort;
    
    private final String intermediaryAdditionalFilesPath;
    
    private final String destinationAdditionalFilesPath;
    private final String destinationFileServerUsername;
    private final String destinationFileServer;
    private final int destinationFileServerPort;
    
    private final Integer sourceBlogId;
    private final Integer destinationBlogId;
    
    
    
    /**
     * Defaults port to 22
     * 
     * @param sourceAdditionalFilesPath
     * @param sourceFileServerUsername
     * @param sourceFileServer
     * @param intermediaryAdditionalFilesPath
     * @param destinationAdditionalFilesPath
     * @param destinationFileServerUsername
     * @param destinationFileServer
     */
    public CopyFilesR2R(String sourceAdditionalFilesPath, String sourceFileServerUsername, String sourceFileServer, String intermediaryAdditionalFilesPath,
    					 String destinationAdditionalFilesPath, String destinationFileServerUsername, String destinationFileServer, 
    					 Integer sourceBlogId, Integer destinationBlogId)
    {
    	super();
    	
        this.sourceAdditionalFilesPath = sourceAdditionalFilesPath;
        this.sourceFileServerUsername = sourceFileServerUsername;
        this.sourceFileServer = sourceFileServer;
        this.sourceFileServerPort = 22;
        
        this.intermediaryAdditionalFilesPath = intermediaryAdditionalFilesPath;
        
        this.destinationAdditionalFilesPath = destinationAdditionalFilesPath;
        this.destinationFileServerUsername = destinationFileServerUsername;
        this.destinationFileServer = destinationFileServer;
        this.destinationFileServerPort = 22;
        
        this.sourceBlogId = sourceBlogId;
        this.destinationBlogId = destinationBlogId;
    }
    
    
    
    /**
     * @param sourceAdditionalFilesPath
     * @param sourceFileServerUsername
     * @param sourceFileServer
     * @param sourceFileServerPort
     * @param intermediaryAdditionalFilesPath
     * @param destinationAdditionalFilesPath
     * @param destinationFileServerUsername
     * @param destinationFileServer
     * @param destinationFileServerPort
     */
    public CopyFilesR2R(String sourceAdditionalFilesPath, String sourceFileServerUsername, String sourceFileServer, int sourceFileServerPort,
    					 String intermediaryAdditionalFilesPath, String destinationAdditionalFilesPath, String destinationFileServerUsername, 
    					 String destinationFileServer, int destinationFileServerPort, Integer sourceBlogId, Integer destinationBlogId)
    {
    	super();
    	
        this.sourceAdditionalFilesPath = sourceAdditionalFilesPath;
        this.sourceFileServerUsername = sourceFileServerUsername;
        this.sourceFileServer = sourceFileServer;
        this.sourceFileServerPort = sourceFileServerPort;
        
        this.intermediaryAdditionalFilesPath = intermediaryAdditionalFilesPath;
        
        this.destinationAdditionalFilesPath = destinationAdditionalFilesPath;
        this.destinationFileServerUsername = destinationFileServerUsername;
        this.destinationFileServer = destinationFileServer;
        this.destinationFileServerPort = destinationFileServerPort;
        
        
        this.sourceBlogId = sourceBlogId;
        this.destinationBlogId = destinationBlogId;
    }
    
    
    
    public void copy() throws SftpException, IOException, JSchException {
    	try {
			logger.info("Copying additional files to local machine. This may take a few minutes.");
			copyAdditionalFilesToLocal();
		} catch (IOException e) {
			logger.error("Could not delete the intermediary additional files directory from local machine.");
			throw e;
		} 
		catch (JSchException e) {
			logger.error("Could not connect to the source file server.");
			logger.error("For error \"com.jcraft.jsch.JSchException: UnknownHostKey\" try:");
			logger.error("ssh-keyscan -t rsa hostname >> ~/.ssh/known_hosts");
			throw e;
		} catch (SftpException e) {
			logger.error("File expected to be in the source file server could not be found.");
			throw e;
		}
		
		logger.info("Additional files copied to local machine.");
		
		try {
			logger.info("Copying additional files to remote machine. This may take a few minutes.");
			copyAdditionalFilesToRemote();
		} catch (IOException e) {
			logger.error("Could not delete the destination additional files directory.");
			throw e;
		} 
		catch (JSchException e) {
			logger.error("Could not connect to the destination file server.");
			logger.error("For error \"com.jcraft.jsch.JSchException: UnknownHostKey\" try:");
			logger.error("ssh-keyscan -t rsa hostname >> ~/.ssh/known_hosts");
			throw e;
		} catch (SftpException e) {
			logger.error("Could not upload files to the file server.");
			throw e;
		}
		
		logger.info("Additional files copied to remote machine.");
    }
    	
    
    	
    /**
     * Connect to the source file server and pull down the files in the sourceBlogId folder to the local intermediary directory.
     * 
     * @throws IOException for FileUtils.deleteDirectory
     * @throws SftpException for channelSftp.cd() and copyFiles()
     * @throws JSchException for everything else
     */
    public void copyAdditionalFilesToLocal() throws IOException, SftpException, JSchException{
            JSch jsch = new JSch();

            jsch.addIdentity(identityLocation);
            
            jsch.setKnownHosts(knownHostsLocation);
            
            Session session = jsch.getSession(sourceFileServerUsername, sourceFileServer, sourceFileServerPort);
            
            session.connect();
            
            Channel channel = session.openChannel("sftp");
            channel.connect();

            ChannelSftp channelSftp = (ChannelSftp) channel;

    		File destinationMediaDir = new File(destinationAdditionalFilesPath + destinationBlogId);
    		FileUtils.deleteDirectory(destinationMediaDir);
            
    		copyFilesToLocal(channelSftp, sourceAdditionalFilesPath + sourceBlogId, intermediaryAdditionalFilesPath + destinationBlogId);

            channelSftp.exit();
            session.disconnect();
    }
    
    
    
    /**
     * Copy files from a remote directory to a local directory
     * 
     * @param channelSftp The channel of the file server we're pulling from.
     * @param remotePath The path to the files we're pulling.
     * @param localPath The path we should put the files in.
     * @throws SftpException Thrown if a file doesn't exist.
     */
    public static void copyFilesToLocal( ChannelSftp channelSftp, String remotePath, String localPath ) throws SftpException {
		Vector<ChannelSftp.LsEntry> ls = channelSftp.ls(remotePath);
		
		for( ChannelSftp.LsEntry entry : ls ) {
			// Don't follow . or .. directories
			if( !entry.getFilename().matches("\\.\\.?")) {
				// Recursively copy directories
	        	if( entry.getAttrs().isDir() ) {
	        		new File( localPath + "/" + entry.getFilename() ).mkdirs();
	        		copyFilesToLocal( channelSftp, remotePath + "/" + entry.getFilename(), localPath + "/" + entry.getFilename() );
	        	}
	        	// Copy individual files over
	        	else {
	        		channelSftp.cd(remotePath);
	        		channelSftp.lcd(localPath);
	        		
	        		channelSftp.get(entry.getFilename(), entry.getFilename() );
	        	}
			}
		}
	}
    
    
    
    /**
     * Connect to the destination file server and push the files up to the destinationBlogId folder from the local intermediary directory.
     * 
     * @throws IOException for FileUtils.deleteDirectory
     * @throws SftpException for channelSftp.cd() and copyFiles()
     * @throws JSchException for everything else
     */
    public void copyAdditionalFilesToRemote() throws IOException, SftpException, JSchException{
			JSch jsch = new JSch();
	
	        jsch.addIdentity(identityLocation);
	        
	        jsch.setKnownHosts(knownHostsLocation);
	        
	        Session session = jsch.getSession(destinationFileServerUsername, destinationFileServer, destinationFileServerPort);
	        
	        session.connect();
	        
	        Channel channel = session.openChannel("sftp");
	        channel.connect();
	
	        ChannelSftp channelSftp = (ChannelSftp) channel;
	
	        createDirIfNotExists( channelSftp, destinationAdditionalFilesPath + destinationBlogId);
	        rmDirContents( channelSftp, destinationAdditionalFilesPath + destinationBlogId);
	        copyFilesToRemote(channelSftp, intermediaryAdditionalFilesPath + destinationBlogId, destinationAdditionalFilesPath + destinationBlogId);
	
	        channelSftp.exit();
	        session.disconnect();
	        
    		File destinationMediaDir = new File(destinationAdditionalFilesPath + destinationBlogId);
    		FileUtils.deleteDirectory(destinationMediaDir);
    }
    
    
    
    /**
     * Copy files from a local directory to a remote directory
     * 
     * @param channelSftp The channel of the file server we're pulling from.
     * @param remotePath The path to the files we're pulling.
     * @param localPath The path we should put the files in.
     * @throws SftpException Thrown if a file doesn't exist.
     */
    public static void copyFilesToRemote( ChannelSftp channelSftp, String localPath, String remotePath ) throws SftpException {
		File localDir = new File(localPath);
		
		for( File file : localDir.listFiles() ) {
			if( file.isDirectory() ) {
				channelSftp.cd(remotePath);
				channelSftp.mkdir(file.getName());
				copyFilesToRemote( channelSftp, localPath + "/" + file.getName(), remotePath + "/" + file.getName() );
			}
			else {
				channelSftp.cd(remotePath);
				channelSftp.lcd(localPath);
				
				channelSftp.put(file.getName(), file.getName());
			}
		}
	}
    
    
    
    /**
     * Create a remote directory if it doesn't exist
     * 
     * @param sftp The connection to the server
     * @param path The directory to be created
     * @throws SftpException Thrown if there is an issue with the remote connection
     */
    public static void createDirIfNotExists( ChannelSftp sftp, String path ) throws SftpException {
		String[] dirs = path.split("/");
		
		for(String dir : dirs) {
			if( dir.length() == 0 ) {
				sftp.cd("/");
			}
			else {
				try {
					sftp.cd(dir);
				}
				catch(SftpException e) {
					sftp.mkdir(dir);
					sftp.cd(dir);
				}
			}
		}
	}
	
    
    
    /**
     * Remove the contents of a directory on a remote server
     * 
     * @param sftp The connection to the server
     * @param path The directory whose contents should be removed
     * @throws SftpException Thrown if there is an issue with the remote connection
     */
	private static void rmDirContents( ChannelSftp sftp, String path ) throws SftpException {

		Vector<ChannelSftp.LsEntry> ls = sftp.ls(path);
		
		for( ChannelSftp.LsEntry entry : ls ) {
			// Don't follow . or .. directories
			if( !entry.getFilename().matches("\\.\\.?")) {
				// Recursively delete directories
	        	if( entry.getAttrs().isDir() ) {
	        		rmDirContents( sftp, path + "/" + entry.getFilename() );
	        		sftp.cd(path);
	        		sftp.rmdir(entry.getFilename());
	        	}
	        	// Delete individual files
	        	else {
	        		sftp.cd(path);
	        		sftp.rm(entry.getFilename());
	        	}
			}
		}
	}
	
}
