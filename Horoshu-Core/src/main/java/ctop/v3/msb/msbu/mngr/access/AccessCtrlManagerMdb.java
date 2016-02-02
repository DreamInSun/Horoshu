package ctop.v3.msb.msbu.mngr.access;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dom4j.Element;

import ctop.v3.msb.common.urn.MsbDomain;
import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.usrm.data.UsrmNodeInfo;

/**
 * Under Construction
 * 
 * @author DreamInSun
 * 
 */
public class AccessCtrlManagerMdb implements IAccessCtrlManager {

	/*========== MDB Constant ==========*/
	//final static String ACCESS_CTRL_DB_CONNWD = "jdbc:h2:mem:AccessCtrl;";
	final static String ACCESS_CTRL_DB_CONNWD = "jdbc:h2:~/AccessCtrl;AUTO_SERVER=TRUE;";

	final static String ACCESS_CTRL_DB_UNAME = "SA";
	final static String ACCESS_CTRL_DB_PASSWD = "";

	/*========== SQL Command ==========*/
	/* Drop Table Command */
	final static String SQL_CMD_DROP_TABLES = "DROP TABLE AccessCtrl;"; ///////////////

	/* Create Table Command */
	final static String SQL_CMD_INIT_TABLES = /////////////////////////////////////////
	"CREATE TABLE IF NOT EXISTS AccessCtrl  ( " + ////////////////////////////////////////
			"	ACID char(256) PRIMARY KEY, " + /////////////////////////////////////////
			"	TYPE char(256), " + /////////////////////////////////////////////////////
			"	MSBURN char(256), " + ///////////////////////////////////////////////////
			"	CALLER char(128), " + ///////////////////////////////////////////////////
			"	PERMITION boolean" + ////////////////////////////////////////////////////
			")";

	/** Insert Access Control Item to Table */
	final static String SQL_CMD_INSERT_ITEM = "INSERT INTO AccessCtrl (ACID, TYPE, MSBURN, CALLER, PERMITION) VALUES (?, ?, ?, ?, ?);";

	/* Search Command */
	final static String SQL_CMD_SEARCH_BY_MSBURN = "SELECT ACID,TYPE,MSBURN,CALLER,PERMITION FROM AccessCtrl t_acm WHERE t_acm.MSBURN = ?";

	/* Statistic Command */
	final static String SQL_CMD_COUNT_ALL_ITEMS = "SELECT count(*) AS ItemCount FROM AccessCtrl t_acm";

	/*========== Properties ==========*/
	private Connection m_connDb;

	/*========== Properties ==========*/

	/*========== Constructor ==========*/
	public AccessCtrlManagerMdb() {
		try {
			/*===== Establish Connection =====*/
			m_connDb = DriverManager.getConnection(ACCESS_CTRL_DB_CONNWD, ACCESS_CTRL_DB_UNAME, ACCESS_CTRL_DB_PASSWD);

			/*===== Initialize DataBase =====*/
			// TODO ALL INITIALIZE FUNCTION NOT IMPLEMENT
			//dropTables();
			createTables();

			initFromDocument(null);

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				m_connDb.close();
			} catch (Exception ignore) {

			}
		}
	}

	/*========== Constructor Assistant ==========*/
	private void initFromDocument(Element elmt) {

	}

	/*========== Deconstructor ==========*/
	public void finalize() {
		try {
			if (m_connDb != null && m_connDb.isClosed() == false) {
				m_connDb.close();
			}
		} catch (SQLException ignore) {
		}
	}

	/*==============================================================*/
	/*==================== Data Base Management ====================*/
	/*==============================================================*/

	/*========== Export Function : dropTables ==========*/
	public boolean dropTables() throws SQLException {
		int cnt = this.executeUpdate(SQL_CMD_DROP_TABLES);
		return (cnt != 0);
	}

	/*========== Export Function : createTables ==========*/
	private boolean createTables() throws SQLException {
		int cnt = this.executeUpdate(SQL_CMD_INIT_TABLES);
		return (cnt != 0);
	}

	/*=========== Export Function : insertItem ==========*/
	/**
	 * 
	 * @param type
	 * @param msbUrn
	 * @param domain
	 * @return
	 */
	public boolean insertItem(AccessCtrlItem.ECtrlType type, MsbUrn msbUrn, String domain, boolean isPermit) {
		int cnt = 0;
		if (m_connDb != null) {
			PreparedStatement pstmt;
			try {
				/* Create Statement */
				pstmt = m_connDb.prepareStatement(SQL_CMD_INSERT_ITEM);
				/* Fill Arguments */
				pstmt.setString(1, UUID.randomUUID().toString());
				pstmt.setString(2, type.name());
				pstmt.setString(3, msbUrn.fullUrn);
				pstmt.setString(4, domain);
				pstmt.setBoolean(5, isPermit);
				/* */
				cnt = pstmt.executeUpdate();
				/* Close Statement */
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return (cnt != 0);
	}

	/*=========== Export Function : insertItem ==========*/
	/**
	 * 
	 * @param type
	 * @param msbUrn
	 * @param domain
	 * @return
	 */
	public int getCount() {
		int tmpCnt = 0;
		if (m_connDb != null) {
			Statement stmt;
			try {
				/* Create Statement */
				stmt = m_connDb.createStatement();
				ResultSet rs = stmt.executeQuery(SQL_CMD_COUNT_ALL_ITEMS);
				/* Close Statement */
				while (rs.next()) {
					tmpCnt = rs.getInt("ItemCount");
				}
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tmpCnt;
	}

	/*========== Execute Query ==========*/
	public final ResultSet executeQuery(String command) {
		ResultSet rs = null;
		if (m_connDb != null) {
			Statement stm;
			try {
				/* Create Statement */
				stm = m_connDb.createStatement();
				rs = stm.executeQuery(command);
				/* Close Statement */
				stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rs;
	}

	private final int executeUpdate(String command) {
		int cnt = 0;
		if (m_connDb != null) {
			try {
				/* Create Statement */
				Statement stm = m_connDb.createStatement();
				cnt = stm.executeUpdate(command);
				/* Close Statement */
				//stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return cnt;
	}

	/*========== Assistant Function ==========*/
	public List<AccessCtrlItem> selectItemByMsbUrn(MsbUrn msbUrn) {
		List<AccessCtrlItem> tmpAcItemList = new ArrayList<AccessCtrlItem>();
		if (m_connDb != null) {
			PreparedStatement pstmt;
			try {
				/* Create Statement */
				pstmt = m_connDb.prepareStatement(SQL_CMD_SEARCH_BY_MSBURN);
				/* Fill Arguments */
				pstmt.setString(1, msbUrn.fullUrn);
				/* Execute */
				ResultSet rs = pstmt.executeQuery();
				/* Fill Return */
				rs.first();
				while (rs.next()) {
					AccessCtrlItem tmpAcItem = new AccessCtrlItem(MsbUrn.parse(rs.getString("MSBURN")), AccessCtrlItem.ECtrlType.valueOf(rs.getString("TYPE")), rs.getString("CALLER"),
							rs.getBoolean("PERMITION"));
					tmpAcItemList.add(tmpAcItem);
				}
				/* Close Statement */
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tmpAcItemList;
	}

	/*========================================================================*/
	/*==================== Interface : IAccessCtrlManager ====================*/
	/*========================================================================*/

	/*========== Access Control Domain Brief Management ==========*/
	@Override
	public void updateAccessDomainBrief(AccessCtrlBrief acBrief) {
		// TODO Auto-generated method stub
	}

	/*========== Assertion of Access Services ==========*/

	public boolean isPermit(MsbuInfo msbuInfo) {
		return true;
	}

	public boolean isPermit(String domain) {

		return true;
	}

	public boolean isPermit(MsbDomain domain) {

		return true;
	}

	/*=======================================================================*/
	/*==================== Access Control Map Management ====================*/
	/*=======================================================================*/

	/**
	 * 
	 */
	public void addAccessCrtlItem(AccessCtrlItem item) {

	}

	/**
	 * 
	 */
	public void removeAccessCtrlItem(MsbUrn msbUrn) {

	}

	/*====================================================================*/
	/*==================== USRM Management Interfaces ====================*/
	/*====================================================================*/

	/**
	 * for Master to Push Access Control Map.
	 */
	public void replaceAccessCtrlMap() {

	}

	@Override
	public boolean isPermit(MsbUrn msbUrn, String callerDomain) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPermit(MsbUrn msbUrn, UsrmNodeInfo callerInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPermit(UsrmNodeInfo callerInfo) {
		// TODO Auto-generated method stub
		return false;
	}

}
