package com.dbscanner.common;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;

public class DbScanner {

	static ConfigFileReader configFileReader;

	static Connection con = null;
	static Statement stmt = null;
	static ResultSet rs = null;

	static long scanColCount = 0;

	public static ArrayList<String> saveValue = new ArrayList<String>();
	public static ArrayList<ArrayList<String>> lengthSixteenValue = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> track2Value = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> failedTable = new ArrayList<ArrayList<String>>();

	public static ArrayList<ArrayList<String>> cardNumberExisttableName = new ArrayList<ArrayList<String>>();
	public static ArrayList<String> cardNumberNotExisttableName = new ArrayList<String>();
	public static ArrayList<String> allTableName = new ArrayList<String>();
	public static ArrayList<String> failedTableOnly = new ArrayList<String>();

	public static ArrayList<ArrayList<String>> track2ExisttableName = new ArrayList<ArrayList<String>>();
	public static ArrayList<String> track2NotExisttableName = new ArrayList<String>();

	public static ArrayList<ArrayList<String>> masterCard = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> visaCard = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> amexCard = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> jcbCard = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> upiCard = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> maestroCard = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<String>> otherCard = new ArrayList<ArrayList<String>>();

	public static ArrayList<String> updateAttributeList = new ArrayList<String>();
	public static ArrayList<ArrayList<String>> fileArray = new ArrayList<ArrayList<String>>();
	public static ArrayList<String> removeFile = new ArrayList<String>();

	public static long track2Count = 0;

	static String userNameValue = "";
	static String passwordValue = "";
	static String ipValue = "";
	static String portValue = "";
	static String dbNameValue = "";

	public static boolean allColoumnScan = true;

	Logger log = Logger.getLogger("LOG");

	public DbScanner() {
		configFileReader = new ConfigFileReader();
	}

	public void updateUsrPass(String userName, String password) {
		userNameValue = userName;
		passwordValue = password;
	}

	public void updateIP(String ip, String port, String dbName) {
		ipValue = ip;
		portValue = port;
		dbNameValue = dbName;
	}

	public Connection createDB(String alias, String DBType) {
		int i = 0;
		boolean isConnectSuccess = false;
		// String upperAlias = alias.toUpperCase();
		String ip = "";
		while (i < 2) {
			try {
				switch (DBType) {
				case "ORACLE":
					Class.forName("oracle.jdbc.driver.OracleDriver");
					ip = "jdbc:oracle:thin:@" + ipValue + ":" + portValue + ":" + dbNameValue;
					break;
				case "POSTGRE":
					Class.forName("org.postgresql.Driver");
					ip = "jdbc:postgresql://" + ipValue + ":" + portValue + "/" + dbNameValue;
					break;
				case "DB2":
					Class.forName("com.ibm.db2.jcc.DB2Driver");
					ip = "jdbc:db2://" + ipValue + ":" + portValue + "/" + dbNameValue;

					break;
				case "MYSQL":
					Class.forName("com.mysql.jdbc.Driver");
					ip = "jdbc:mysql://" + ipValue + ":" + portValue + "/" + dbNameValue;
					break;

				}

				con = DriverManager.getConnection(ip, userNameValue, passwordValue);

				i = i + 2;
				isConnectSuccess = true;

			} catch (PSQLException e) {
				isConnectSuccess = false;
				log.error(e.getMessage(), e);
				e.printStackTrace();
				i++;
			} catch (SQLException e1) {
				isConnectSuccess = false;
				log.error(e1.getMessage(), e1);
				e1.printStackTrace();
				i++;
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
				isConnectSuccess = false;
				e2.printStackTrace();
				i = i + 2;
			}
		}
		if (!isConnectSuccess) {
			System.exit(0);
		}
		return con;
	}

	public boolean verfyDb(String alias, String query, String rowCount, String DBType) {

		int count = 0;
		boolean isSuccess = false;
		Statement stmt = null;
		ResultSet rs = null;

		if (con == null) {
			con = createDB(alias, DBType);
		}

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (PSQLException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (SQLException e1) {
			log.error(e1.getMessage(), e1);
			e1.printStackTrace();
		} catch (Exception e2) {
			log.error(e2.getMessage(), e2);
			e2.printStackTrace();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
		}
		isSuccess = (count == Integer.parseInt(rowCount));
		return isSuccess;
	}

	public ArrayList<String> getTableValue(String alias, String query, String ColumnName, String DBType) {
		Statement stmt = null;
		ResultSet rs = null;

		if (con == null) {
			con = createDB(alias, DBType);
		}

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);

			saveValue = new ArrayList<>();
			while (rs.next()) {
				if (rs.getString(ColumnName) != null
						&& !configFileReader.getRemoveTable().contains(rs.getString(ColumnName))) {
					saveValue.add(rs.getString(ColumnName));
				}
			}
		} catch (PSQLException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (SQLException e1) {
			log.error(e1.getMessage(), e1);
			e1.printStackTrace();
		} catch (Exception e2) {
			log.error(e2.getMessage(), e2);
			;
			e2.printStackTrace();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
		}

		return saveValue;
	}

	public ArrayList<String> getDbValue(String alias, String query, String ColumnName, String DBType, String tblName) {
		Statement stmt = null;
		ResultSet rs = null;

		if (con == null) {
			con = createDB(alias, DBType);
		}

		ArrayList<String> tmpFailedTable = new ArrayList<String>();

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			saveValue = new ArrayList<>();
			while (rs.next()) {
				if (rs.getString(ColumnName) != null) {
					saveValue.add(rs.getString(ColumnName));
				}
			}
		} catch (PSQLException e) {
			tmpFailedTable.add(tblName);
			tmpFailedTable.add("ALL");
			if (!failedTable.contains(tmpFailedTable)) {
				failedTable.add(tmpFailedTable);
			}
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (SQLException e1) {
			tmpFailedTable.add(tblName);
			tmpFailedTable.add("ALL");
			if (!failedTable.contains(tmpFailedTable)) {
				failedTable.add(tmpFailedTable);
			}
			log.debug("Query is - "+query);
			log.error(e1.getMessage(), e1);
			e1.printStackTrace();
		} catch (Exception e2) {
			log.error(e2.getMessage(), e2);
			e2.printStackTrace();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
		}

		return saveValue;
	}

	public void isValidationValue(String alias, String query, String ColumnName, String table, int indexNumber,
			String DBType) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;

		if (con == null) {
			con = createDB(alias, DBType);
		}

		ArrayList<String> tmpFailedTable = new ArrayList<String>();

		try {

			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				indexNumber++;
				scanColCount++;
				try {
					if (!(rs.getString(ColumnName) == null || rs.getString(ColumnName).equals(" "))) {
						extractvalues(rs.getString(ColumnName), ColumnName, table, indexNumber);
					}
				} catch (SQLException e) {
				}
			}

		} catch (PSQLException e1) {
			allColoumnScan = false;
			tmpFailedTable.add(table);
			tmpFailedTable.add(ColumnName);
			if (!failedTable.contains(tmpFailedTable)) {
				failedTable.add(tmpFailedTable);
			}
			log.error(e1.getMessage(), e1);
			e1.printStackTrace();
		} catch (SQLException e2) {
			allColoumnScan = false;
			tmpFailedTable.add(table);
			tmpFailedTable.add(ColumnName);
			if (!failedTable.contains(tmpFailedTable)) {
				failedTable.add(tmpFailedTable);
			}
			log.error(e2.getMessage(), e2);
			e2.printStackTrace();
		} catch (Exception e3) {
			log.error(e3.getMessage(), e3);
			e3.printStackTrace();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e4) {
					e4.printStackTrace();
				}
		}

	}

	public void closeDBConnection() {
		if (con != null)
			try {
				con.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			}
		if (stmt != null)
			try {
				stmt.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			}
		if (rs != null)
			try {
				rs.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			}
	}

	public void updateAttrtFunction(ArrayList<String> attribute) {
		for (int i = 0; i < attribute.size(); i++) {
			updateAttributeList.add(attribute.get(i));
		}
	}

	public static void extractvalues(String s, String columnName, String tableName, int indexNumber) {

		if (updateAttributeList.contains("PLAINCARDNUMBER")) {
			searchPlainCardNumber(s, columnName, tableName, indexNumber);
		}
		if (updateAttributeList.contains("TRACK2")) {
			searchTrack2(s, columnName, tableName, indexNumber);
		}
	}

	public static void searchPlainCardNumber(String s, String columnName, String tableName, int indexNumber) {
		Pattern p = Pattern.compile("[\\d]+");
		Matcher m = p.matcher(s);
		boolean isDigit = false;
		boolean isCharX = true;

		while (m.find()) {
			String tempattributeValue = m.group();
			String attributeValue = m.group().replaceAll("\\s+", "");
			int avSize = attributeValue.length();
			if (!attributeValue.equals("") && (avSize == 16 || avSize == 17 || avSize == 18 || avSize == 19)) {
				int x = s.indexOf(tempattributeValue);
				int z = x + tempattributeValue.length();
				if (x - 2 >= 0) {
					char c = s.substring(x - 2, x - 1).charAt(0);
					isDigit = Character.isDigit(c);
					isCharX = false;
				}

				if (isCharX || !s.substring(x - 1, x).equals(".") || !isDigit) {
					if (!(z + 1 <= s.length()) || !s.substring(z, z + 1).equals(".")) {
						ArrayList<String> lengthSixteenValueDetails = new ArrayList<String>();
						lengthSixteenValueDetails.add(m.group());
						lengthSixteenValueDetails.add(tableName);
						lengthSixteenValueDetails.add(columnName);
						lengthSixteenValueDetails.add(Integer.toString(indexNumber));
						lengthSixteenValue.add(lengthSixteenValueDetails);
					}
				}
			}
		}
	}

	public static void searchTrack2(String s, String columnName, String tableName, int indexNumber) {
		Pattern p = Pattern.compile("\\d+=\\d+");
		Matcher m = p.matcher(s);
		while (m.find()) {
			if ((m.group().indexOf("=") == 16) || (m.group().indexOf("=") == 17) || (m.group().indexOf("=") == 18)
					|| (m.group().indexOf("=") == 19)) {
				ArrayList<String> track2ValueDetails = new ArrayList<String>();
				track2ValueDetails.add(tableName);
				track2ValueDetails.add(columnName);
				track2ValueDetails.add(m.group());
				track2ValueDetails.add(Integer.toString(indexNumber));
				track2Value.add(track2ValueDetails);
				track2Count++;
			}
		}
	}

	public void isCardNumber() {

		try {

			String masterCardRegex = "((5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12})";
			String visaCardRegex = "(4[0-9]{12}[0-9]{3})";
			String amexCardRegex = "(347[0-9]{13})";
			String jcbCardRegex = "((2131|1800|35{3})[0-9]{12})";
			String upiCardRegex = "(62[0-9]{14,17})";
			String maestroCardRegex = "((5018|5020|5038|6304|6759|6761|6763)[0-9]{8,15})";

			for (int k = 0; k < lengthSixteenValue.size(); k++) {

				ArrayList<String> masterCardDetails = new ArrayList<String>();
				ArrayList<String> visaCardDetails = new ArrayList<String>();
				ArrayList<String> amexCardDetails = new ArrayList<String>();
				ArrayList<String> jcbCardDetails = new ArrayList<String>();
				ArrayList<String> upiCardDetails = new ArrayList<String>();
				ArrayList<String> maestroCardDetails = new ArrayList<String>();
				ArrayList<String> otherCardDetails = new ArrayList<String>();

				boolean notRecognizeIndicator = true;
				if (lengthSixteenValue.get(k).get(0).matches(masterCardRegex)) {
					masterCardDetails.add(lengthSixteenValue.get(k).get(1));
					masterCardDetails.add(lengthSixteenValue.get(k).get(2));
					masterCardDetails.add(lengthSixteenValue.get(k).get(0));
					masterCardDetails.add(lengthSixteenValue.get(k).get(3));
					masterCard.add(masterCardDetails);
					notRecognizeIndicator = false;
				} else if (lengthSixteenValue.get(k).get(0).matches(visaCardRegex)) {
					visaCardDetails.add(lengthSixteenValue.get(k).get(1));
					visaCardDetails.add(lengthSixteenValue.get(k).get(2));
					visaCardDetails.add(lengthSixteenValue.get(k).get(0));
					visaCardDetails.add(lengthSixteenValue.get(k).get(3));
					visaCard.add(visaCardDetails);
					notRecognizeIndicator = false;
				} else if (lengthSixteenValue.get(k).get(0).matches(amexCardRegex)) {
					amexCardDetails.add(lengthSixteenValue.get(k).get(1));
					amexCardDetails.add(lengthSixteenValue.get(k).get(2));
					amexCardDetails.add(lengthSixteenValue.get(k).get(0));
					amexCardDetails.add(lengthSixteenValue.get(k).get(3));
					amexCard.add(amexCardDetails);
					notRecognizeIndicator = false;
				} else if (lengthSixteenValue.get(k).get(0).matches(jcbCardRegex)) {
					jcbCardDetails.add(lengthSixteenValue.get(k).get(1));
					jcbCardDetails.add(lengthSixteenValue.get(k).get(2));
					jcbCardDetails.add(lengthSixteenValue.get(k).get(0));
					jcbCardDetails.add(lengthSixteenValue.get(k).get(3));
					jcbCard.add(jcbCardDetails);
					notRecognizeIndicator = false;
				} else if (lengthSixteenValue.get(k).get(0).matches(upiCardRegex)) {
					upiCardDetails.add(lengthSixteenValue.get(k).get(1));
					upiCardDetails.add(lengthSixteenValue.get(k).get(2));
					upiCardDetails.add(lengthSixteenValue.get(k).get(0));
					upiCardDetails.add(lengthSixteenValue.get(k).get(3));
					upiCard.add(upiCardDetails);
					notRecognizeIndicator = false;
				} else if (lengthSixteenValue.get(k).get(0).matches(maestroCardRegex)) {
					maestroCardDetails.add(lengthSixteenValue.get(k).get(1));
					maestroCardDetails.add(lengthSixteenValue.get(k).get(2));
					maestroCardDetails.add(lengthSixteenValue.get(k).get(0));
					maestroCardDetails.add(lengthSixteenValue.get(k).get(3));
					maestroCard.add(maestroCardDetails);
					notRecognizeIndicator = false;
				} else if (notRecognizeIndicator) {
					boolean isFound = false;
					for (String a : configFileReader.getBin()) {
						if (lengthSixteenValue.get(k).get(0).startsWith(a)) {
							otherCardDetails.add(lengthSixteenValue.get(k).get(1));
							otherCardDetails.add(lengthSixteenValue.get(k).get(2));
							otherCardDetails.add(lengthSixteenValue.get(k).get(0));
							otherCardDetails.add(lengthSixteenValue.get(k).get(3));
							otherCard.add(otherCardDetails);
							isFound = true;
							break;
						}
					}
					if (!isFound) {
						lengthSixteenValue.remove(k);
						k = k - 1;
					}

				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public static boolean existPlanCardNumber() {
		boolean existPlanCardNumberIndicator;
		if (!masterCard.isEmpty() || !visaCard.isEmpty() || !amexCard.isEmpty() || !jcbCard.isEmpty()
				|| !upiCard.isEmpty() || maestroCard.isEmpty())
			existPlanCardNumberIndicator = true;
		else
			existPlanCardNumberIndicator = false;
		return existPlanCardNumberIndicator;
	}

	public ArrayList<Integer> getCount(int count) {
		ArrayList<Integer> countArray = new ArrayList<Integer>();
		int limitRowCount = configFileReader.getLimitRowCount();
		int a = 0;
		int b = 0;
		a = count / limitRowCount;
		b = count % limitRowCount;
		if (a > 0) {
			for (int i = 1; i <= a; i++) {
				countArray.add(i * limitRowCount);
			}
			countArray.add(count);
		} else {
			countArray.add(b);
		}
		return countArray;
	}

	public String limitQuery(String table, String coloumn, int limitValue, String constant, String DBType) {
		String query = "";
		String dbTyp = DBType;
		if (constant == "a") {
			switch (dbTyp) {
			case "ORACLE":
				query = "SELECT " + coloumn + " FROM " + table + " OFFSET 0 ROWS FETCH NEXT " + limitValue
						+ " ROWS ONLY";
				break;
			case "POSTGRE":
				query = "select " + coloumn + " from " + table + " LIMIT " + limitValue + " offset 0";
				break;
			case "MYSQL":
				query = "select " + coloumn + " from " + table + " LIMIT " + limitValue;
				break;
			}
		} else {
			switch (dbTyp) {
			case "ORACLE":
				query = "SELECT " + coloumn + " FROM " + table + " OFFSET " + limitValue + " ROWS FETCH NEXT "
						+ configFileReader.getLimitRowCount() + " ROWS ONLY";
				break;
			case "POSTGRE":
				query = "select " + coloumn + " from " + table + " LIMIT " + configFileReader.getLimitRowCount()
						+ " offset " + limitValue;
				break;
			case "MYSQL":
				query = "select " + coloumn + " from " + table + " LIMIT " + limitValue + ","
						+ configFileReader.getLimitRowCount();
				break;
			}
		}
		return query;
	}

	public void saveAllTableName(ArrayList<String> allTableArray) {
		for (int i = 0; i < allTableArray.size(); i++) {
			allTableName.add(allTableArray.get(i));
		}
	}

	public void updatecardNumberNotExisttableName() {
		ArrayList<String> cardNumberOnlyExisttableName = new ArrayList<String>();

		for (int j = 0; j < cardNumberExisttableName.size(); j++) {
			if (!cardNumberOnlyExisttableName.contains(cardNumberExisttableName.get(j).get(0))) {
				cardNumberOnlyExisttableName.add(cardNumberExisttableName.get(j).get(0));
			}
		}

		for (int i = 0; i < allTableName.size(); i++) {
			if (!cardNumberOnlyExisttableName.contains(allTableName.get(i))
					&& !failedTableOnly.contains(allTableName.get(i))) {
				cardNumberNotExisttableName.add(allTableName.get(i));
			}
		}

	}

	public void updateTrc2NotExisttableName() {

		ArrayList<String> track2OnlyExisttableName = new ArrayList<String>();

		for (int j = 0; j < track2ExisttableName.size(); j++) {
			if (j == 0) {
				track2OnlyExisttableName.add(track2ExisttableName.get(j).get(0));
			} else {
				if (!track2OnlyExisttableName.contains(track2ExisttableName.get(j).get(0))) {
					track2OnlyExisttableName.add(track2ExisttableName.get(j).get(0));
				}
			}
		}

		for (int i = 0; i < allTableName.size(); i++) {
			if (!track2OnlyExisttableName.contains(allTableName.get(i))
					&& !failedTableOnly.contains(allTableName.get(i))) {
				track2NotExisttableName.add(allTableName.get(i));
			}
		}
	}

	public void updateCardNumberExisttableName() {
		try {
			ArrayList<String> tmpCrdNumExisttblName = new ArrayList<String>();
			for (int i = 0; i < lengthSixteenValue.size(); i++) {
				tmpCrdNumExisttblName = new ArrayList<String>();
				tmpCrdNumExisttblName.add(lengthSixteenValue.get(i).get(1));
				tmpCrdNumExisttblName.add(lengthSixteenValue.get(i).get(2));
				if (!cardNumberExisttableName.contains(tmpCrdNumExisttblName)) {
					cardNumberExisttableName.add(tmpCrdNumExisttblName);
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public void updatefailedTableOnly() {
		try {
			ArrayList<String> tmpCrdNumExisttblName = new ArrayList<String>();
			for (int i = 0; i < failedTable.size(); i++) {
				if (!failedTableOnly.contains(failedTable.get(i).get(0))) {
					failedTableOnly.add(failedTable.get(i).get(0));
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public void updatetrack2ExisttableName() {
		String tempColoumn = "";
		String tempTYable = "";
		for (int i = 0; i < track2Value.size(); i++) {
			ArrayList<String> tempArray = new ArrayList<String>();
			if (i == 0) {
				tempTYable = track2Value.get(i).get(0);
				tempArray.add(tempTYable);
				tempColoumn = track2Value.get(i).get(1);
				tempArray.add(tempColoumn);
				track2ExisttableName.add(tempArray);
			} else {
				if (track2Value.get(i).get(2) != tempColoumn || track2Value.get(i).get(1) != tempTYable) {
					tempTYable = track2Value.get(i).get(0);
					tempArray.add(tempTYable);
					tempColoumn = track2Value.get(i).get(1);
					tempArray.add(tempColoumn);
					track2ExisttableName.add(tempArray);
				}

			}
		}
	}

	public int plcrdpageCount() {
		int[] a = { masterCard.size(), visaCard.size(), amexCard.size(), jcbCard.size(), upiCard.size(),
				maestroCard.size(), otherCard.size() };
		List b = Arrays.asList(ArrayUtils.toObject(a));
		return (int) Collections.max(b);
	}

	public void getFileList() {
		File file = new File("config/");
		File[] fileList = file.listFiles();
		ArrayList<String> fileArrayDetails = new ArrayList<String>();
		double totalFileSize = 0;
		for (int i = 0; i < fileList.length; i++) {
			if (FilenameUtils.isExtension(fileList[i].toString().toUpperCase(), "XLS")) {
				double fileSize = fileList[i].length() / 1048576;
				if (fileSize > 25) {
					removeFile.add(fileList[i].toString());
				} else {
					totalFileSize = totalFileSize + fileSize;
					if (totalFileSize < 25) {
						fileArrayDetails.add(fileList[i].toString());
					} else {
						fileArray.add(fileArrayDetails);
						fileArrayDetails = new ArrayList<String>();
						fileArrayDetails.add(fileList[i].toString());
						totalFileSize = fileSize;
					}
				}
			}
		}
		if (!fileArrayDetails.isEmpty()) {
			fileArray.add(fileArrayDetails);
		}

	}

}
