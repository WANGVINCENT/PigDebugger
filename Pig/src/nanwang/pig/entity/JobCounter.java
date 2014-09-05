package nanwang.pig.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This is the Job Entity
 * @author wangnan
 * @since 2014-05-01
 */

@Entity 
@Table (name="counter")
public class JobCounter {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column (name="id")
	private int id;
	
	@Column (name="script_name")
	private String name;
	
	@Column (name="jobRankNum")
	private int jobRankNum;

	@Column (name="mapNum")
	private int mapNum;
	
	@Column (name="reduceNum")
	private int reduceNum;
	
	@Column (name="mapCPUTime")
	private int mapCPUTime;
	
	@Column (name="reduceCPUTime")
	private int reduceCPUTime;
	
	@Column (name="totalCPUTime")
	private int totalCPUTime;
	
	@Column (name="mapElapsedTime")
	private double mapElapsedTime;
	
	@Column (name="reduceElapsedTime")
	private double reduceElapsedTime;

	@Column (name="shufflePhaseTime")
	private int shufflePhaseTime;
	
	@Column (name="sortPhaseTime")
	private int sortPhaseTime;
	
	@Column (name="reducePhaseTime")
	private int reducePhaseTime;
	
	@Column (name="shuffleBytes")
	private int shuffleBytes;
	
	public JobCounter(){
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMapNum() {
		return mapNum;
	}

	public void setMapNum(int mapNum) {
		this.mapNum = mapNum;
	}

	public int getReduceNum() {
		return reduceNum;
	}

	public void setReduceNum(int reduceNum) {
		this.reduceNum = reduceNum;
	}

	public int getMapCPUTime() {
		return mapCPUTime;
	}

	public void setMapCPUTime(int mapCPUTime) {
		this.mapCPUTime = mapCPUTime;
	}

	public int getReduceCPUTime() {
		return reduceCPUTime;
	}

	public void setReduceCPUTime(int reduceCPUTime) {
		this.reduceCPUTime = reduceCPUTime;
	}

	public int getTotalCPUTime() {
		return totalCPUTime;
	}

	public void setTotalCPUTime(int totalCPUTime) {
		this.totalCPUTime = totalCPUTime;
	}

	public double getMapElapsedTime() {
		return mapElapsedTime;
	}

	public void setMapElapsedTime(double mapElapsedTime) {
		this.mapElapsedTime = mapElapsedTime;
	}

	public double getReduceElapsedTime() {
		return reduceElapsedTime;
	}

	public void setReduceElapsedTime(double reduceElapsedTime) {
		this.reduceElapsedTime = reduceElapsedTime;
	}

	public int getShufflePhaseTime() {
		return shufflePhaseTime;
	}

	public void setShufflePhaseTime(int shufflePhaseTime) {
		this.shufflePhaseTime = shufflePhaseTime;
	}

	public int getSortPhaseTime() {
		return sortPhaseTime;
	}

	public void setSortPhaseTime(int sortPhaseTime) {
		this.sortPhaseTime = sortPhaseTime;
	}

	public int getReducePhaseTime() {
		return reducePhaseTime;
	}

	public void setReducePhaseTime(int reducePhaseTime) {
		this.reducePhaseTime = reducePhaseTime;
	}

	public int getShuffleBytes() {
		return shuffleBytes;
	}

	public void setShuffleBytes(int shuffleBytes) {
		this.shuffleBytes = shuffleBytes;
	}
	
	public int getJobRankNum() {
		return jobRankNum;
	}

	public void setJobRankNum(int jobRankNum) {
		this.jobRankNum = jobRankNum;
	}
}
