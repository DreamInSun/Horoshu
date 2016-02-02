package ctop.v3.msb.proxy.routemap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.usrm.data.*;

/**
 * MSB resource router based on In-Memory Data Base. <br />
 * Using H2 as core RDBMS. <br />
 * 
 * @author DreamInSun
 * 
 */
public class RouteMapMdb extends RouteMapBase {

	/*========== Constant ==========*/
	final static String ROUTE_DB_CONNWD = "jdbc:h2:mem:RouteMap";
	final static String ROUTE_DB_UNAME = "SA";
	final static String ROUTE_DB_PASSWD = "";

	/*========== SQL Command ==========*/
	/* Create Table Command */
	final static String SQL_CMD_INIT_TABLES = //////////////////////////////////////////
	"CREATE TABLE RouteInfo( " + ////////////////////////////////////////////////////////
			"	MSBURN char(256) PRIMARY KEY" + /////////////////////////////////////////
			"	URN char(128) , " + /////////////////////////////////////////////////////
			"	VER_MAJOR int, " + //////////////////////////////////////////////////////
			"	VER_MINOR int, " + //////////////////////////////////////////////////////
			"	VER_SUFFIX char(8),  " + ////////////////////////////////////////////////
			"	MSB_ID char(128), " + ///////////////////////////////////////////////////
			"	PROXY_ID char(128), " + /////////////////////////////////////////////////
			"	UPDATE_TIME timestamp" + ////////////////////////////////////////////////
			")";

	/* Insert Data to Table */
	final static String SQL_CMD_INSERT_ITEM = "INSERT INTO RouteInfo " + "(MSBURN, URN, VER_MAJOR, VER_MINOR, VER_SUFFIX, MSB_ID, PROXY_ID, UPDATE_TIME ) " + "VALUES () ";

	/* Search Command */
	final static String SQL_CMD_SEARCH = "";

	/*========== Properties ==========*/
	private Connection m_connDb;
	private Statement m_stmt;

	/*========== Constructor ==========*/
	public RouteMapMdb(MsbuInfo msbuInfo) {
		super(msbuInfo);
		try {
			/*===== Establish Connection =====*/
			m_connDb = DriverManager.getConnection(ROUTE_DB_CONNWD, ROUTE_DB_UNAME, ROUTE_DB_PASSWD);
			m_stmt = m_connDb.createStatement();

			/*===== Initialize DataBase =====*/
			createTables();

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				m_stmt.close();
				m_connDb.close();
			} catch (Exception ignore) {

			}
		}
	}

	/*========== Deconstructor ==========*/
	public void finalize() {
		try {
			m_connDb.close();
		} catch (SQLException ignore) {
		}
	}

	/*==============================================================*/
	/*==================== Data Base Management ====================*/
	/*==============================================================*/

	/*========== Assistant Function ==========*/
	private void createTables() throws SQLException {
		if (m_stmt != null) {
			m_stmt.execute(SQL_CMD_INIT_TABLES);
		}
	}

	/*=================================================================*/
	/*==================== Interface : IUrsmRouter ====================*/
	/*=================================================================*/

	/*========== IUrsmRouter : toMsbRouteBulletin ==========*/
	@Override
	public MsbRouteBulletin toMsbRouteBulletin() {
		// TODO Auto-generated method stub
		return null;
	}

	/*========== IUrsmRouter : addRouteItem ==========*/
	@Override
	public void addRouteItem(MsbRouteItem routeItem) {
		// TODO Auto-generated method stub
	}

	/*========== IUrsmRouter : deleteRouteItem ==========*/
	@Override
	public void removeRouteItem(MsbUrn msbUrn) {
		// TODO Auto-generated method stub
	}

	/*========== IUrsmRouter : updateServicesMap ==========*/
	@Override
	public void updateServicesMap(MsbRouteBulletin bulletin) {
		// TODO Auto-generated method stub

	}

	/*========== IUrsmRouter : updateServicesMap ==========*/
	@Override
	public MsbRouteItem searchRouteItem(MsbUrn urn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected MsbRouteItem discoverRoute(MsbUrn svUrn, String param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}
}
