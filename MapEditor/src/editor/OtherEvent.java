package editor;


import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;

class PlayMusic
	extends Operation {

	private int musicID;

	public PlayMusic(int id) {
		super(id, PLAY_MUSIC);
		init();
	}

	public PlayMusic(int id, int type) {
		super(id, type);
		init();
	}

	private void init() {
		musicID = 0;
	}

	public int getMusicID() {
		return musicID;
	}

	public void setMusicID(int musicID) {
		this.musicID = musicID;
	}

	public void saveMobileData(DataOutputStream out, StringManager stringManager) throws Exception {
		super.saveMobileData(out, stringManager);
		SL.writeInt(musicID, out);
	}

	public void save(DataOutputStream out, StringManager stringManager) throws Exception {
		super.save(out, stringManager);
		out.writeInt(musicID);
	}

	protected void load(DataInputStream in, StringManager stringManager) throws Exception {
		super.load(in, stringManager);
		musicID = in.readInt();
	}

	public String getListItemDescription() {
		String result = "播放第" + musicID + "号音乐";
		return result;
	}

	public Operation getCopy() {
		PlayMusic result = (PlayMusic) (Operation.createInstance(this.id, this.type));
		result.musicID = this.musicID;
		return result;
	}
}

class PlayMusicSetter
	extends OperationSetter {

	private PlayMusic playMusic;
	private NumberSpinner musicSpinner;

	public PlayMusicSetter(JDialog owner, MainFrame mainFrame, PlayMusic playMusic) {
		super(owner, mainFrame);
		this.setTitle("设置播放音乐");
		this.playMusic = playMusic;
		this.mainFrame = mainFrame;

		musicSpinner = new NumberSpinner();
		musicSpinner.setIntValue(playMusic.getMusicID());

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		Container cp = this.getContentPane();
		cp.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 2, 3, 3);
		cp.add(new JLabel("音乐编号："), c);

		c.gridx = 1;
		c.weightx = 1;
		cp.add(musicSpinner, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.weighty = 0;
		cp.add(buttonPanel, c);
	}

	public Operation getOperation() {
		return playMusic;
	}

	public void okPerformed() {
		playMusic.setMusicID(musicSpinner.getIntValue());
		this.closeType = OK_PERFORMED;
		dispose();
	}

	public void cancelPerformed() {
		dispose();
	}
}


class PlayAnim
	extends Operation {

	private int animID;

	public PlayAnim(int id) {
		super(id, PLAY_ANIM);
		init();
	}

	public PlayAnim(int id, int type) {
		super(id, type);
		init();
	}

	private void init() {
		animID = 0;
	}

	public int getAnimID() {
		return animID;
	}

	public void setAnimID(int animID) {
		this.animID = animID;
	}

	public void saveMobileData(DataOutputStream out, StringManager stringManager) throws Exception {
		super.saveMobileData(out, stringManager);
		SL.writeInt(animID, out);
	}

	public void save(DataOutputStream out, StringManager stringManager) throws Exception {
		super.save(out, stringManager);
		out.writeInt(animID);
	}

	protected void load(DataInputStream in, StringManager stringManager) throws Exception {
		super.load(in, stringManager);
		animID = in.readInt();
	}

	public String getListItemDescription() {
		String result = "播放第" + animID + "号动画";
		return result;
	}

	public Operation getCopy() {
		PlayAnim result = (PlayAnim) (Operation.createInstance(this.id, this.type));
		result.animID = this.animID;
		return result;
	}
}

class PlayAnimSetter
	extends OperationSetter {

	private PlayAnim playAnim;
	private NumberSpinner animSpinner;

	public PlayAnimSetter(JDialog owner, MainFrame mainFrame, PlayAnim playAnim) {
		super(owner, mainFrame);
		this.setTitle("设置播放动画");
		this.playAnim = playAnim;
		this.mainFrame = mainFrame;

		animSpinner = new NumberSpinner();
		animSpinner.setIntValue(playAnim.getAnimID());

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		Container cp = this.getContentPane();
		cp.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 2, 3, 3);
		cp.add(new JLabel("动画编号："), c);
		
		c.gridx = 1;
		c.weightx = 1;
		cp.add(animSpinner, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.weighty = 0;
		cp.add(buttonPanel, c);
	}

	public Operation getOperation() {
		return playAnim;
	}

	public void okPerformed() {
		playAnim.setAnimID(animSpinner.getIntValue());
		this.closeType = OK_PERFORMED;
		dispose();
	}

	public void cancelPerformed() {
		dispose();
	}
}


class ChangeMap
	extends Operation {

	private int stageID;
	private int mapID;

	public ChangeMap(int id) {
		super(id, CHANGE_MAP);
		init();
	}

	public ChangeMap(int id, int type) {
		super(id, type);
		init();
	}

	private void init() {
		stageID = 0;
		mapID = 0;
	}

	public int getStageID() {
		return stageID;
	}

	public void setStageID(int stageID) {
		this.stageID = stageID;
	}
	
	public int getMapID() {
		return mapID;
	}
	
	public void setMapID(int mapID) {
		this.mapID = mapID;
	}

	public void saveMobileData(DataOutputStream out, StringManager stringManager) throws Exception {
		super.saveMobileData(out, stringManager);
		SL.writeInt(stageID, out);
		SL.writeInt(mapID, out);
	}

	public void save(DataOutputStream out, StringManager stringManager) throws Exception {
		super.save(out, stringManager);
		out.writeInt(stageID);
		out.writeInt(mapID);
	}

	protected void load(DataInputStream in, StringManager stringManager) throws Exception {
		super.load(in, stringManager);
		stageID = in.readInt();
		mapID = in.readInt();
	}

	public String getListItemDescription() {
		String result = "切换到第" + stageID + "大关第" + mapID + "小节";
		return result;
	}

	public Operation getCopy() {
		ChangeMap result = (ChangeMap) (Operation.createInstance(this.id, this.type));
		result.stageID = this.stageID;
		result.mapID = this.mapID;
		return result;
	}
}

class ChangeMapSetter
	extends OperationSetter {

	private ChangeMap changeMap;
	private NumberSpinner stageSpinner;
	private NumberSpinner mapSpinner;

	public ChangeMapSetter(JDialog owner, MainFrame mainFrame, ChangeMap changeMap) {
		super(owner, mainFrame);
		this.setTitle("设置切换地图");
		this.changeMap = changeMap;
		this.mainFrame = mainFrame;

		stageSpinner = new NumberSpinner();
		stageSpinner.setIntValue(changeMap.getStageID());
		
		mapSpinner = new NumberSpinner();
		mapSpinner.setIntValue(changeMap.getMapID());

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		Container cp = this.getContentPane();
		cp.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 2, 3, 3);
		cp.add(new JLabel("大关："), c);

		c.gridx = 1;
		c.weightx = 1;
		cp.add(stageSpinner, c);
		
		c.gridx = 2;
		c.weightx = 0;
		cp.add(new JLabel("小节："), c);
		
		c.gridx = 3;
		c.weightx = 1;
		cp.add(mapSpinner, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 4;
		c.weighty = 0;
		cp.add(buttonPanel, c);
	}

	public Operation getOperation() {
		return changeMap;
	}

	public void okPerformed() {
		changeMap.setStageID(stageSpinner.getIntValue());
		changeMap.setMapID(mapSpinner.getIntValue());
		this.closeType = OK_PERFORMED;
		dispose();
	}

	public void cancelPerformed() {
		dispose();
	}
}


class ChangeState
	extends Operation {
	
	public final static int PASS_STAGE = 0;
	public final static int PASS_THROUGH = 1;
	public final static int GAME_OVER = 2;
	
	public final static int[] STATES = {
									   PASS_STAGE, 
									   PASS_THROUGH, 
									   GAME_OVER
	};
	
	public final static String[] DESCS = {
										 "过关", 
										 "通关",
										 "游戏结束"
	};
	
	public final static String getDesc(int state) {
		String result = "未知状态";
		if(state >= 0 && state < STATES.length) {
			result = "状态[" + DESCS[state] + "]";
		}
		return result;
	}
	

	private int state;

	public ChangeState(int id) {
		super(id, CHANGE_STATE);
		init();
	}

	public ChangeState(int id, int type) {
		super(id, type);
		init();
	}

	private void init() {
		state = 0;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void saveMobileData(DataOutputStream out, StringManager stringManager) throws Exception {
		super.saveMobileData(out, stringManager);
		SL.writeInt(state, out);
	}

	public void save(DataOutputStream out, StringManager stringManager) throws Exception {
		super.save(out, stringManager);
		out.writeInt(state);
	}

	protected void load(DataInputStream in, StringManager stringManager) throws Exception {
		super.load(in, stringManager);
		state = in.readInt();
	}

	public String getListItemDescription() {
		String result = "切换到" + ChangeState.getDesc(state);
		return result;
	}

	public Operation getCopy() {
		ChangeState result = (ChangeState) (Operation.createInstance(this.id, this.type));
		result.state = this.state;
		return result;
	}
}

class ChangeStateSetter
	extends OperationSetter {

	private ChangeState changeState;
	private ValueChooser stateChooser;

	public ChangeStateSetter(JDialog owner, MainFrame mainFrame, ChangeState changeState) {
		super(owner, mainFrame);
		this.setTitle("设置改变状态");
		this.changeState = changeState;
		this.mainFrame = mainFrame;

		stateChooser = new ValueChooser(changeState.getState(), ChangeState.STATES, ChangeState.DESCS);		

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		Container cp = this.getContentPane();
		cp.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 2, 3, 3);
		cp.add(new JLabel("状态："), c);

		c.gridx = 1;
		c.weightx = 1;
		cp.add(stateChooser, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.weighty = 0;
		cp.add(buttonPanel, c);
	}

	public Operation getOperation() {
		return changeState;
	}

	public void okPerformed() {
		changeState.setState(stateChooser.getValue());
		this.closeType = OK_PERFORMED;
		dispose();
	}

	public void cancelPerformed() {
		dispose();
	}
}
