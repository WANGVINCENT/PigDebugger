package nanwang.pig.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * This is the Output entity
 * @author wangnan
 * @since 2014-05-01
 */

@Entity (name="output")
public class Output {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column (name="id")
	private int id;
	
	@Column (name="script_uuid")
	private String uuid;
	
	@Column (name="name")
	private String name;
	
	@Column (name="time")
	private String time;
	
	@Column (name="state")
	private String state;
	
	@Column (name="output", length = 2147483647, columnDefinition="LONGTEXT")
	private String output;
	
	public Output(String uuid, StringBuilder output, String name, String state, String time){
		this.uuid = uuid;
		this.output = output.toString();
		this.name = name;
		this.state = state;
		this.time = time;
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

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
