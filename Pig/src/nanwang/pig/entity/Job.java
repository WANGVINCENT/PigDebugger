package nanwang.pig.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * This is the Job Entity
 * @author wangnan
 * @since 2014-05-01
 */

@Entity (name="job")
public class Job {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column (name="id")
	private int id;
	
	@Column (name="script_uuid")
	private String uuid;
	
	@Column (name="operation", length = 65535, columnDefinition="TEXT")
	private String operation;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
}
