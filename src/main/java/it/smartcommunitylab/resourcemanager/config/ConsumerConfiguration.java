package it.smartcommunitylab.resourcemanager.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import it.smartcommunitylab.resourcemanager.SystemKeys;

@Component
@ConfigurationProperties(prefix = "consumers.static")
public class ConsumerConfiguration {

	private List<String> sql = new ArrayList<String>();
	private List<String> nosql = new ArrayList<String>();
	private List<String> file = new ArrayList<String>();
	private List<String> object = new ArrayList<String>();

	public List<String> getSql() {
		return sql;
	}

	public void setSql(List<String> sql) {
		this.sql = sql;
	}

	public List<String> getNosql() {
		return nosql;
	}

	public void setNosql(List<String> nosql) {
		this.nosql = nosql;
	}

	public List<String> getFile() {
		return file;
	}

	public void setFile(List<String> file) {
		this.file = file;
	}

	public List<String> getObject() {
		return object;
	}

	public void setObject(List<String> object) {
		this.object = object;
	}

	public List<String> get(String type) {
		List<String> res = null;
		switch (type) {
		case SystemKeys.TYPE_SQL:
			res = getSql();
			break;
		case SystemKeys.TYPE_NOSQL:
			res = getNosql();
			break;
		case SystemKeys.TYPE_FILE:
			res = getFile();
			break;
		case SystemKeys.TYPE_OBJECT:
			res = getObject();
			break;
		}

		return res;
	}

}
