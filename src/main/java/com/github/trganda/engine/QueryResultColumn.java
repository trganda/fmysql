/*
 * Copyright 2022 paxos.cn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.trganda.engine;

/** A column of the result */
public class QueryResultColumn {

    private String name;
    private String type;

    public QueryResultColumn() {}

    public QueryResultColumn(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return Column name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Column name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Data type like 'varchar(255)'
     */
    public String getType() {
        return type;
    }

    /**
     * @param type Data type
     */
    public void setType(String type) {
        this.type = type;
    }
}
