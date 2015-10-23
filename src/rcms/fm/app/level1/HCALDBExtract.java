import java.io.*;
import java.util.*;
import java.sql.*;

import rcms.util.logger.RCMSLogger;

public class HCALDBExtract
{

    static RCMSLogger logger = new RCMSLogger(HCALDBExtract.class);

    // In c++ this would be a typedef. Alas, this is java...
    public class SnippetMap extends HashMap<String, String> { private static final long serialVersionUID = 1L; }

    // Attributes:
    private SnippetMap _m_snippet = new SnippetMap();
    private SnippetMap _m_version = new SnippetMap();

    // Static attributes (DB stuff):
    private static String _url = "jdbc:oracle:thin:@( DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = cmsonr1-v)(PORT =10121)) (ADDRESS = (PROTOCOL = TCP)(HOST = cmsonr2-v)(PORT =10121)) (ADDRESS = (PROTOCOL = TCP)(HOST = cmsonr3-v)(PORT =10121)) (ADDRESS = (PROTOCOL = TCP)(HOST = cmsonr4-v)(PORT =10121)) (ADDRESS = (PROTOCOL = TCP)(HOST = cmsonr5-v)(PORT =10121)) (ADDRESS = (PROTOCOL = TCP)(HOST = cmsonr6-v)(PORT =10121)) (ENABLE=BROKEN) (LOAD_BALANCE = yes) (CONNECT_DATA = (SERVER = DEDICATED) (SERVICE_NAME = cms_omds_lb.cern.ch) (FAILOVER_MODE = (TYPE = SELECT)(METHOD = BASIC)(RETRIES = 200)(DELAY= 15)) ) )";
    private static String _userid = "CMS_HCL_SNIPPET_W";
    private static String _passwd_file = "/nfshome0/hcalcfg/cvs2db_scripts/.hcal_db_passwd_production";

    //////////////////////
    //// Constructor: ////
    //////////////////////

    public HCALDBExtract( SnippetMap path2tag )
    {

	//
	//// Read password from file: ////
	//

	String password = null;
	try
	    {
		BufferedReader in = new BufferedReader( new FileReader( _passwd_file ) );
		password = in.readLine();
		in.close();
	    }
	catch( IOException e )
	    {	
		String errMessage = "[HCAL base] Error! IOException: Cannot read DB password ...";
		logger.error(errMessage,e);
	    }

	//
	//// Get snippet for given paths and tags from the database and store them in maps: ////
	//

	try
	    {

		Class.forName( "oracle.jdbc.driver.OracleDriver" );
		Connection con = DriverManager.getConnection( _url, _userid, password );

		if( con == null )
		    {
			System.err.println( "Null connection" );
			System.exit(1);
		    }

		Statement st = con.createStatement();

		for( String path: path2tag.keySet() )
		    {

			String tag = path2tag.get( path );

			String q = "select SNIPPET_REVISION,SNIPPET_DATA_CLOB from CMS_HCL_SNIPPET.V_TAG_SNIPPET_REVISIONS where SNIPPET_TAG='" + tag + "' AND SNIPPET_PATH='" + path + "'";

			ResultSet rset = st.executeQuery(q);

			while ( rset.next() )
			    {
				String version = rset.getString(1);
				String snippet = rset.getString(2);
				_m_snippet.put( path, snippet );
				_m_version.put( path, version );
			    }
		    }

		st.close();
		con.close();

	    }
	catch( Exception e )
	    {
		String errMessage = "[HCAL base] Error! Exception: Cannot read anything from the DB ...";
		logger.error(errMessage,e);

	    }

    }

    ////////////////////
    //// Accessors: ////
    ////////////////////

    public String snippet( String path ) { return _m_snippet.get( path ); }
    public String version( String path ) { return _m_version.get( path ); }

    ////////////////////
    //// Dump maps: ////
    ////////////////////

    public void dump_maps()
    {

	for( String path: _m_snippet.keySet() )
	    {
		String snippet = _m_snippet.get( path );
		String version = _m_version.get( path );
		System.out.println( ">>>>>>>>>> Path " + path + ", version " + version + ": <<<<<<<<<<" );
		System.out.println( snippet );
	    }

    }

}
