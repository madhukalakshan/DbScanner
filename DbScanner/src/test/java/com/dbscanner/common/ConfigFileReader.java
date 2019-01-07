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

	public String getDateFormat() {
		String dateFormat = properties.getProperty("DATEFORMAT");
		if (dateFormat != null)
			return dateFormat;
		else
			throw new RuntimeException("DATEFORMAT not specified in the Configuration.properties file.");
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
	
	public String getSchemaName() {
		String schemaName = properties.getProperty("SCHEMANAME");
		if (schemaName != null)
			return schemaName;
		else
			throw new RuntimeException("SCHEMANAME not specified in the Configuration.properties file.");
	}
	
}
