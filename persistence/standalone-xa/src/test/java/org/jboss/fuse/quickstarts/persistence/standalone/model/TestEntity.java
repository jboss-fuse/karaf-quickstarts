/*
 *  Copyright 2005-2021 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.jboss.fuse.quickstarts.persistence.standalone.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="TEST_ENTITY")
public class TestEntity {
	@Id
	@Column(name="ID")
	private Long id;

	@Column(name="DESCRIPTION")
	private String description;

	public TestEntity() {
	}

	public TestEntity(Long id, String description) {
		this.id = id;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TestEntity that = (TestEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, description);
	}

	@Override
	public String toString() {
		return "TestEntity [id=" + id + ", description=" + description + "]";
	}

}
