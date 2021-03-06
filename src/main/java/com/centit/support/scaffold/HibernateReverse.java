package com.centit.support.scaffold;

import com.centit.support.database.metadata.*;
import com.centit.support.database.utils.DBType;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DbcpConnectPools;

import java.sql.Connection;
import java.sql.SQLException;

public class HibernateReverse {

	public static void runTask(TaskDesc task ) throws SQLException {
		if(! task.isRunHibernateReverse())
			return ;
		
		String sJavaSourceDir = task.getProjDir()+'/'+task.getSrcDir();
		String sClassPath = task.getAppPackagePath() + "/po";
		String sPackageName = sClassPath.replace('/', '.');
		String sTableNames = task.getTableNames();
		
		String [] sTables = sTableNames.split(",");
		
		DataSourceDescription dataSource = task.getDataSourceDesc();
		 
		Connection dbc= DbcpConnectPools.getDbcpConnect(dataSource);
		
		DatabaseMetadata db = null;
		
		if( dataSource.getConnUrl().indexOf("oracle")>=0)
			db = new OracleMetadata();
		else if( dataSource.getConnUrl().indexOf("db2")>=0)
			db = new DB2Metadata();
		else if( dataSource.getConnUrl().indexOf("sqlserver")>=0)
			db = new SqlSvrMetadata();
		else {
			System.out.println("无法辨认数据库类型！");
			return ;
		}
		
		db.setDBConfig(dbc);
		try {
			db.setDBSchema(dbc.getSchema());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		for(int i=0;i<sTables.length;i++ ){
			String sTableName = sTables[i].toUpperCase();
			SimpleTableInfo tm = db.getTableMetadata(sTableName);
			tm.setPackageName(sPackageName);
			
			tm.saveHibernateMappingFile( sJavaSourceDir +"/"+sClassPath+"/"+tm.getClassName()+".hbm.xml");
			System.out.println("转换"+sTableName+"已完成！");
		}
	}
	
	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {

		//DBConn.loadHibernateConfig("E:/Study/Centit/BSDFW/web/WEB-INF/hibernate.cfg.xml");
		if( args.length < 4 ){
			System.out.println("缺少参数！");
			return;
		}
		
		String sJavaSourceDir = args[0];
		String sClassPath = args[1] + "/po";
		String sPackageName = sClassPath.replace('/', '.');
		String sTableNames = args[2];
		String sHibernateConfigFile = args[3];
		
		String [] sTables = sTableNames.split(",");
		String sDbBeanName = "dataSource";
		if( args.length > 4 ) 
			sDbBeanName = args[4];
		
		DataSourceDescription dataSource=new DataSourceDescription();
		dataSource.loadHibernateConfig(sHibernateConfigFile,sDbBeanName);

		Connection dbc= DbcpConnectPools.getDbcpConnect(dataSource);
		
		DatabaseMetadata db = DatabaseMetadata.createDatabaseMetadata(
				DBType.mapDBType(dataSource.getConnUrl())
		);
		
		db.setDBConfig(dbc);
		
		try {
			db.setDBSchema( dbc.getSchema() );
		} catch (SQLException e) {
		}
		
		for(int i=0;i<sTables.length;i++ ){
			String sTableName = sTables[i].toUpperCase();
			SimpleTableInfo tm = db.getTableMetadata(sTableName);
			tm.setPackageName(sPackageName);
			
			tm.saveHibernateMappingFile( sJavaSourceDir +"/"+sClassPath+"/"+tm.getClassName()+".hbm.xml");
			System.out.println("转换"+sTableName+"已完成！");
		}
	}

}
