package com.dbscanner.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class ConfigFileReader {

	private Properties properties;
	private final String propertyFilePath = "config/Configuration.properties";

	public ConfigFileReader() {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(propertyFilePath));
			properties = new Properties();

			try {
				properties.load(reader);
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Configuration.properties not found at " + propertyFilePath);
		}
	}

	public String getDBType() {
		String DBType = properties.getProperty("DBType");
		if (DBType != null)
			return DBType;
		else
			throw new RuntimeException("DBType not specified in the Configuration.properties file.");
	}

	public String getSwtUserName() {
		String swtUserName = properties.getProperty("SWTUSERNAME");
		if (swtUserName != null)
			return swtUserName;
		else
			throw new RuntimeException("SWTUSERNAME not specified in the Configuration.properties file.");
	}

	public String getSwtPassword() {
		String swtPassword = properties.getProperty("SWTPASSWORD");
		if (swtPassword != null)
			return swtPassword;
		else
			throw new RuntimeException("SWTPASSWORD not specified in the Configuration.properties file.");
	}

	public String getBknUserName() {
		String bknUserName = properties.getProperty("BKNUSERNAME");
		if (bknUserName != null)
			return bknUserName;
		else
			throw new RuntimeException("BKNUSERNAME not specified in the Configuration.properties file.");
	}

	public String getBknPassword() {
		String bknPassword = properties.getProperty("BKNPASSWORD");
		if (bknPassword != null)
			return bknPassword;
		else
			throw new RuntimeException("BKNPASSWORD not specified in the Configuration.properties file.");
	}

	public String getIpgUserName() {
		String ipgUserName = properties.getProperty("IPGUSERNAME");
		if (ipgUserName != null)
			return ipgUserName;
		else
			throw new RuntimeException("IPGUSERNAME not specified in the Configuration.properties file.");
	}

	public String getIpgPassword() {
		String ipgPassword = properties.getProperty("IPGPASSWORD");
		if (ipgPassword != null)
			return ipgPassword;
		else
			throw new RuntimeException("IPGPASSWORD not specified in the Configuration.properties file.");
	}

	public String getUsrName() {
		String usrName = properties.getProperty("USRUSERNAME");
		if (usrName != null)
			return usrName;
		else
			throw new RuntimeException("USRUSERNAME not specified in the Configuration.properties file.");
	}

	public String getUsrPassword() {
		String usrPassword = properties.getProperty("USRPASSWORD");
		if (usrPassword != null)
			return usrPassword;
		else
			throw new RuntimeException("USRPASSWORD not specified in the Configuration.properties file.");
	}

	public String getBnkUserName() {
		String bnkUserName = properties.getProperty("BNKUSERNAME");
		if (bnkUserName != null)
			return bnkUserName;
		else
			throw new RuntimeException("BNKUSERNAME not specified in the Configuration.properties file.");
	}

	public String getBnkPassword() {
		String bnkPassword = properties.getProperty("BNKPASSWORD");
		if (bnkPassword != null)
			return bnkPassword;
		else
			throw new RuntimeException("BNKPASSWORD not specified in the Configuration.properties file.");
	}

	public String getCardUserName() {
		String crduserName = properties.getProperty("CRDUSERNAME");
		if (crduserName != null)
			return crduserName;
		else
			throw new RuntimeException("CRDUSERNAME not specified in the Configuration.properties file.");
	}

	public String getCardPassword() {
		String crdpwd = properties.getProperty("CRDPASSWORD");
		if (crdpwd != null)
			return crdpwd;
		else
			throw new RuntimeException("CRDPASSWORD not specified in the Configuration.properties file.");
	}

	public String getReportUserName() {
		String rptuserName = properties.getProperty("RPTUSERNAME");
		if (rptuserName != null)
			return rptuserName;
		else
			throw new RuntimeException("RPTUSERNAME not specified in the Configuration.properties file.");
	}

	public String getReportPassword() {
		String rptuserName = properties.getProperty("RPTPASSWORD");
		if (rptuserName != null)
			return rptuserName;
		else
			throw new RuntimeException("RPTPASSWORD not specified in the Configuration.properties file.");
	}

	public String getAltUserName() {
		String altuserName = properties.getProperty("ALTUSERNAME");
		if (altuserName != null)
			return altuserName;
		else
			throw new RuntimeException("ALTUSERNAME not specified in the Configuration.properties file.");
	}

	public String getAltPassword() {
		String altuserName = properties.getProperty("ALTPASSWORD");
		if (altuserName != null)
			return altuserName;
		else
			throw new RuntimeException("ALTPASSWORD not specified in the Configuration.properties file.");
	}

	public String getDefaultUserName() {
		String defaultuserName = properties.getProperty("DEFAULTUSERNAME");
		if (defaultuserName != null)
			return defaultuserName;
		else
			throw new RuntimeException("DEFAULTUSERNAME not specified in the Configuration.properties file.");
	}

	public String getDefaultPassword() {
		String defaultpwd = properties.getProperty("DEFAULTPASSWORD");
		if (defaultpwd != null)
			return defaultpwd;
		else
			throw new RuntimeException("DEFAULTPASSWORD not specified in the Configuration.properties file.");
	}

	public String getDefaultSchema() {
		String defaultSchema = properties.getProperty("DEFAULTSCHEMA");
		if (defaultSchema != null)
			return defaultSchema;
		else
			throw new RuntimeException("DEFAULTSCHEMA not specified in the Configuration.properties file.");
	}

	public String getIP() {
		String url = properties.getProperty("IP");
		if (url != null)
			return url;
		else
			throw new RuntimeException("url not specified in the Configuration.properties file.");
	}

	public String getDateFormat() {
		String dateFormat = properties.getProperty("DATEFORMAT");
		if (dateFormat != null)
			return dateFormat;
		else
			throw new RuntimeException("DATEFORMAT not specified in the Configuration.properties file.");
	}

	public String getLogPropertyFilePath() {
		String logPropertyFilePath = properties.getProperty("LOGPROPERTYFILEPATH");
		if (logPropertyFilePath != null)
			return logPropertyFilePath;
		else
			throw new RuntimeException("LOGPROPERTYFILEPATH not specified in the Configuration.properties file.");
	}

	public String getReportDataFilePathPlanCard() {
		String reptDatFilePathPlnCrd = properties.getProperty("REPORTDATAFILEPATHPLANCARD");
		if (reptDatFilePathPlnCrd != null) {
			return reptDatFilePathPlnCrd;
		} else
			throw new RuntimeException(
					"REPORTDATAFILEPATHPLANCARD not specified in the Configuration.properties file.");
	}

	public String getReportDataFilePathTrack2() {
		String reptDatFilePathtrack2 = properties.getProperty("REPORTDATAFILEPATHTRACK2");
		if (reptDatFilePathtrack2 != null) {
			return reptDatFilePathtrack2;
		} else
			throw new RuntimeException("REPORTDATAFILEPATHTRACK2 not specified in the Configuration.properties file.");
	}

	public String getLoginEmailUserName() {
		String loginEmailUserName = properties.getProperty("LOGINEMAILUSERNAME");
		if (loginEmailUserName != null)
			return loginEmailUserName;
		else
			throw new RuntimeException("LOGINEMAILUSERNAME not specified in the Configuration.properties file.");
	}

	public String getLoginEmailPassword() {
		String loginEmailPassword = properties.getProperty("LOGINEMAILPASSWORD");
		if (loginEmailPassword != null)
			return loginEmailPassword;
		else
			throw new RuntimeException("LOGINEMAILPASSWORD not specified in the Configuration.properties file.");
	}

	public String getToEmailUserName() {
		String toEmailUserName = properties.getProperty("TOEMAILUSERNAME");
		if (toEmailUserName != null)
			return toEmailUserName;
		else
			throw new RuntimeException("TOEMAILUSERNAME not specified in the Configuration.properties file.");
	}

	public ArrayList<String> getRemoveTable() {
		String isAllowAbsoluteResult = properties.getProperty("REMOVETABLE");
		ArrayList<String> removeTable = new ArrayList<String>();
		String[] sparatedValue = isAllowAbsoluteResult.split(",");
		for (int i = 0; i < sparatedValue.length; i++) {
			removeTable.add(sparatedValue[i]);
		}
		if (isAllowAbsoluteResult != null)
			return removeTable;
		else
			throw new RuntimeException("REMOVETABLE not specified in the Configuration.properties file.");
	}

	public int getLimitRowCount() {
		String limitRowCount = properties.getProperty("LIMITROWCOUNT");
		if (limitRowCount != null) {
			return Integer.parseInt(limitRowCount);
		} else
			throw new RuntimeException("LIMITROWCOUNT not specified in the Configuration.properties file.");
	}

	public ArrayList<String> getBin() {
		String binValue = properties.getProperty("BIN");
		ArrayList<String> removeTable = new ArrayList<String>();
		String[] sparatedValue = binValue.split(",");
		for (int i = 0; i < sparatedValue.length; i++) {
			removeTable.add(sparatedValue[i]);
		}
		if (binValue != null)
			return removeTable;
		else
			throw new RuntimeException("BIN not specified in the Configuration.properties file.");
	}

	public int getpageCountConfg() {
		String pageCount = properties.getProperty("PAGECOUNT");
		if (pageCount != null)
			return Integer.parseInt(pageCount);
		else
			throw new RuntimeException("PAGECOUNT not specified in the Configuration.properties file.");
	}

}
