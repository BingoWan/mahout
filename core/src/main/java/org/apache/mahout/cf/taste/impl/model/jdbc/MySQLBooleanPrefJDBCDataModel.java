/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.cf.taste.impl.model.jdbc;

import org.apache.mahout.cf.taste.common.TasteException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * <p>See also {@link MySQLJDBCDataModel} -- same except deals with a table without preference info:</p>
 *
 * <pre>
 * CREATE TABLE taste_preferences (
 *   user_id VARCHAR(10) NOT NULL,
 *   item_id VARCHAR(10) NOT NULL,
 *   PRIMARY KEY (user_id, item_id),
 *   INDEX (user_id),
 *   INDEX (item_id)
 * )
 * </pre>
 */
public class MySQLBooleanPrefJDBCDataModel extends AbstractBooleanPrefJDBCDataModel {

  private static final String NO_SUCH_COLUMN = "NO_SUCH_COLUMN";

  /**
   * <p>Creates a {@link MySQLBooleanPrefJDBCDataModel} using the default {@link javax.sql.DataSource}
   * (named {@link #DEFAULT_DATASOURCE_NAME} and default table/column names.</p>
   *
   * @throws org.apache.mahout.cf.taste.common.TasteException if {@link javax.sql.DataSource} can't be found
   */
  public MySQLBooleanPrefJDBCDataModel() throws TasteException {
    this(DEFAULT_DATASOURCE_NAME);
  }

  /**
   * <p>Creates a {@link MySQLBooleanPrefJDBCDataModel} using the default {@link javax.sql.DataSource}
   * found under the given name, and using default table/column names.</p>
   *
   * @param dataSourceName name of {@link javax.sql.DataSource} to look up
   * @throws org.apache.mahout.cf.taste.common.TasteException if {@link javax.sql.DataSource} can't be found
   */
  public MySQLBooleanPrefJDBCDataModel(String dataSourceName) throws TasteException {
    this(lookupDataSource(dataSourceName),
         DEFAULT_PREFERENCE_TABLE,
         DEFAULT_USER_ID_COLUMN,
         DEFAULT_ITEM_ID_COLUMN);
  }

  /**
   * <p>Creates a {@link MySQLBooleanPrefJDBCDataModel} using the given {@link javax.sql.DataSource}
   * and default table/column names.</p>
   *
   * @param dataSource {@link javax.sql.DataSource} to use
   */
  public MySQLBooleanPrefJDBCDataModel(DataSource dataSource) {
    this(dataSource,
         DEFAULT_PREFERENCE_TABLE,
         DEFAULT_USER_ID_COLUMN,
         DEFAULT_ITEM_ID_COLUMN);
  }

  /**
   * <p>Creates a {@link MySQLBooleanPrefJDBCDataModel} using the given {@link javax.sql.DataSource}
   * and default table/column names.</p>
   *
   * @param dataSource {@link javax.sql.DataSource} to use
   * @param preferenceTable name of table containing preference data
   * @param userIDColumn user ID column name
   * @param itemIDColumn item ID column name
   */
  public MySQLBooleanPrefJDBCDataModel(DataSource dataSource,
                                       String preferenceTable,
                                       String userIDColumn,
                                       String itemIDColumn) {
    super(dataSource,
          preferenceTable,
          userIDColumn,
          itemIDColumn,
          NO_SUCH_COLUMN,
          // getUserSQL
          "SELECT " + itemIDColumn + " FROM " + preferenceTable + " WHERE " + userIDColumn + "=?",
          // getNumItemsSQL
          "SELECT COUNT(DISTINCT " + itemIDColumn + ") FROM " + preferenceTable,
          // getNumUsersSQL
          "SELECT COUNT(DISTINCT " + userIDColumn + ") FROM " + preferenceTable,
          // setPreferenceSQL
          "INSERT IGNORE INTO " + preferenceTable + '(' + userIDColumn + ',' + itemIDColumn + ") VALUES (?,?)",
          // removePreference SQL
          "DELETE FROM " + preferenceTable + " WHERE " + userIDColumn + "=? AND " + itemIDColumn + "=?",
          // getUsersSQL
          "SELECT " + itemIDColumn + ", " + userIDColumn + " FROM " + preferenceTable + " ORDER BY " + userIDColumn,
          // getItemsSQL
          "SELECT DISTINCT " + itemIDColumn + " FROM " + preferenceTable + " ORDER BY " + itemIDColumn,
          // getItemSQL
          "SELECT 1 FROM " + preferenceTable + " WHERE " + itemIDColumn + "=?",
          // getPrefsForItemSQL
          "SELECT " + userIDColumn + " FROM " +
          preferenceTable + " WHERE " + itemIDColumn + "=? ORDER BY " + userIDColumn,
          // getNumPreferenceForItemSQL
          "SELECT COUNT(1) FROM " + preferenceTable + " WHERE " + itemIDColumn + "=?",
          // getNumPreferenceForItemsSQL
          "SELECT COUNT(1) FROM " + preferenceTable + " tp1 INNER JOIN " + preferenceTable + " tp2 " +
          "ON (tp1." + userIDColumn + "=tp2." + userIDColumn + ") " +
          "WHERE tp1." + itemIDColumn + "=? and tp2." + itemIDColumn + "=?");
  }

  @Override
  protected int getFetchSize() {
    // Need to return this for MySQL Connector/J to make it use streaming mode
    return Integer.MIN_VALUE;
  }

  @Override
  protected void advanceResultSet(ResultSet resultSet, int n) throws SQLException {
    // Can't use relative on MySQL Connector/J
    int i = 0;
    while (i < n && resultSet.next()) {
      i++;
    }
  }

}