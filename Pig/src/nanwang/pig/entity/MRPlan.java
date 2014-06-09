package nanwang.pig.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * This is the MRPlan Entity
 * @author wangnan
 * @since 2014-05-01
 */

@Entity (name="mrplan")
public class MRPlan {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column (name="id")
	private int id;
	
	@Column (name="script_uuid")
	private String uuid;
	
	@Column (name="plan", length = 2147483647, columnDefinition="LONGTEXT")
	private String plan;
	
	public MRPlan(String uuid, String plan){
		this.uuid = uuid;
		this.plan = plan;
	}

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

	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}
}
