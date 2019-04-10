package cbp.copyblogs.copyfiles;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import cbp.copyblogs.BlogCopier;

/**
 * 
 * @author Christian Parker
 *
 */
public abstract class CopyFiles {
	protected static Logger logger;
	
	public CopyFiles() {
		PropertyConfigurator.configure(BlogCopier.getConfigLocation() + "log4j.properties");
		CopyFiles.logger = LoggerFactory.getLogger(CopyFiles.class);
	}
	public abstract void copy() throws IOException, SftpException, JSchException;
}
