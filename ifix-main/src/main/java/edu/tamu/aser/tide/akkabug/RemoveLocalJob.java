package edu.tamu.aser.tide.akkabug;

import java.util.ArrayList;

import edu.tamu.aser.tide.shb.Trace;

public class RemoveLocalJob {

	ArrayList<Trace> node;

	public RemoveLocalJob(ArrayList<Trace> team1) {
		// TODO Auto-generated constructor stub
		this.node = team1;
	}

	public ArrayList<Trace> getTeam(){
		return node;
	}

}
