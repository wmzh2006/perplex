package editor;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.*;
import javax.imageio.ImageIO;

public class MainFrame
	extends JFrame {
	public static Font DEF_FONT = new Font("Dialog", Font.PLAIN, 12);

	public static void main(String[] args) {
		try {
			DefaultTheme theme = new DefaultTheme();
			MetalLookAndFeel.setCurrentTheme(theme);
			DEF_FONT = theme.getDefaultFont();
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e, "��ʼ������", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		MainFrame mainFrame = new MainFrame();
	}

	public final static int RES_COUNT = 4;
	public final static int RES_SI = 0;
	public final static int RES_AR = 1;
	public final static int RES_UR = 2;
	public final static int RES_BR = 3;

	public final static int LAYER_COUNT = 6;
	public final static int LAYER_FLOOR = 0;
	public final static int LAYER_TREE = 1;
	public final static int LAYER_BUILDING = 2;
	public final static int LAYER_UNIT = 3;
	public final static int LAYER_WA = 4;
	public final static int LAYER_RECT = 5;
	public final static String[] LAYER_NAMES = {"�ذ�", "���ڵ���", "����", "��ս��λ", "���߷�Χ", "������Χ"};
	public final static String[] TREE_NAMES = LAYER_NAMES;

	public static MainFrame self;
	
	private ProgressDialog progress;
	
	private Object[] resManagers;

	private MouseInfo mouseInfo;
	private MapInfo mapInfo;

	//main component
	private PainterTree[] trees;
	private SpriteManagerPanel[] panels;
	private JTabbedPane treeTab;
	private JTabbedPane panelTab;
	private SpriteInfoPanel infoPanel;

	//event
	private EventManager eventManager;

	//menus
	private JMenuBar menuBar;
	//menu file
	private JMenu menuFile;
	private JMenuItem menuFileNew;
	private JMenuItem menuFileLoad;
	private JMenuItem menuFileSave;
	private JMenuItem menuFileSavePNG;
	//menu edit
	private JMenu menuEdit;
	private JMenuItem menuEditMapSize;
	private JMenuItem menuEditFlipHorizontal;
	private JMenuItem menuEditFlipVertical;
	//menu manager
	private JMenu menuManager;
	private JMenuItem menuEventManager;
	private JMenuItem menuItemManager;

	//toolbar
	private JToolBar toolBar;
	private JButton toolFlip;
	private class RangeButton
		extends JToggleButton {
		private int range;
		public RangeButton(final int range) {
			super("" + range);
			this.range = range;
			setActionCommand("FLOORRANGE");
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (mouseInfo != null) {
						mouseInfo.setRange(range);
					}
				}
			});
		}
	}

	private RangeButton[] toolRanges;
	private JToggleButton toolShowAlpha;
	private JSlider toolAlphaValue;
	private JButton toolCoverable, toolUncoverable;
	private MenuActionListener menuActionListener;
	
	public MainFrame() {
		self = this;
		setTitle("MB3Editor");
		try {
			setIconImage(XImage.readPNG(new File(".\\icon.png")));
		}
		catch (Exception e) {
		}
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(new Dimension(1000, 800));
		setLocationRelativeTo(null);

		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		try {
			init();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws Exception {
		//��ʼ��������
		progress = new ProgressDialog(null);
		progress.setTitle("��������");
		progress.setInfo("Loading");
		progress.setValue(0);
		progress.show();
		
		//������Դ
		initResManagers();
		mouseInfo = new MouseInfo();
		mapInfo = new MapInfo(XUtil.getDefPropInt("DefMapWidth"),
							  XUtil.getDefPropInt("DefMapHeight"),
							  XUtil.getDefPropStr("DefMapName"));
		menuActionListener = new MenuActionListener();

		//���ظ����ؼ�
		progress.setInfo("��ʼ���ؼ�");
		initTrees();
		initPanels();
		initEvents();
		initTab();
		initInfo();
		initMenu();
		initToolBar();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeTab, panelTab);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(200);
		
		( (BRManager) (resManagers[RES_BR])).load("", resManagers);
		refreshBR();

		( (URManager) (resManagers[RES_UR])).load("", (ARManager) (resManagers[RES_AR]));
		refreshUR();

		int index = 0;
		treeTab.setSelectedIndex(index);
		tabChanged();

		this.setJMenuBar(menuBar);
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(toolBar, BorderLayout.NORTH);
		cp.add(splitPane, BorderLayout.CENTER);
		cp.add(infoPanel, BorderLayout.SOUTH);
		
		progress.setValue(100);
		progress.hide();
		show();
	}

	private void initResManagers() throws Exception {
		resManagers = new Object[RES_COUNT];
		
		progress.setInfo("��ȡ��һͼƬ");
		resManagers[RES_SI] = new SIManager();
		progress.setValue(20);
		
		progress.setInfo("��ȡ����");
		resManagers[RES_AR] = new ARManager();
		progress.setValue(60);
		
		progress.setInfo("��ȡ��ս��λ��Դ");
		resManagers[RES_UR] = new URManager();
		progress.setValue(65);
		
		progress.setInfo("��ȡ������Դ");
		resManagers[RES_BR] = new BRManager();
		progress.setValue(70);
	}

	private void initTrees() {
		trees = new PainterTree[LAYER_COUNT];

		trees[LAYER_FLOOR] = new PainterTree(TREE_NAMES[LAYER_FLOOR],
										  SIPainterGroup.getGroups( ( (SIManager) (resManagers[RES_SI])).getGroups()),
										  SIPainter.getPainters( ( (SIManager) (resManagers[RES_SI])).getSIs()),
										  mouseInfo);
	    
		trees[LAYER_TREE] = new PainterTree(TREE_NAMES[LAYER_TREE],
										  SIPainterGroup.getGroups( ( (SIManager) (resManagers[RES_SI])).getGroups()),
										  SIPainter.getPainters( ( (SIManager) (resManagers[RES_SI])).getSIs()),
										  mouseInfo);
		
		trees[LAYER_BUILDING] = new PainterTree(TREE_NAMES[LAYER_BUILDING],
											null,
											BRPainter.getPainters( ( (BRManager) (resManagers[RES_BR])).getBRs()),
											mouseInfo);

		trees[LAYER_UNIT] = new PainterTree(TREE_NAMES[LAYER_UNIT],
											null,
											URPainter.getPainters( ( (URManager) (resManagers[RES_UR])).getURs()),
											mouseInfo);

		trees[LAYER_BUILDING].addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				buildingTreeMouseClicked(e);
			}
		});

		trees[LAYER_UNIT].addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				unitTreeMouseClicked(e);
			}
		});


		trees[LAYER_WA] = new PainterTree(TREE_NAMES[LAYER_WA],
										  null,
										  WAPainter.getPainters(),
										  mouseInfo);

		trees[LAYER_RECT] = new PainterTree(TREE_NAMES[LAYER_RECT],
											null,
											RectPainter.getPainters(),
											mouseInfo);
	}

	private void initPanels() {
		panels = new SpriteManagerPanel[LAYER_COUNT];
		panels[LAYER_FLOOR] = new FloorPanel(this, mouseInfo);
		panels[LAYER_TREE] = new TreeSpritePanel(this, mouseInfo);
		panels[LAYER_BUILDING] = new BuildingPanel(this, mouseInfo);
		panels[LAYER_UNIT] = new UnitPanel(this, mouseInfo);
		panels[LAYER_WA] = new WAPanel(this, mouseInfo);
		panels[LAYER_RECT] = new RectPanel(this, mouseInfo);

		for (int i = 0; i < panels.length; ++i) {
			for (int j = 0; j < i; ++j) {
				if (j == LAYER_WA || j == LAYER_RECT) {
					continue;
				}
				panels[i].addBackManager(panels[j].getManager());
			}
			for (int j = i + 1; j < panels.length; ++j) {
				if (j == LAYER_WA || j == LAYER_RECT) {
					continue;
				}
				panels[i].addForeManager(panels[j].getManager());
			}
		}
	}

	private void initEvents() {
		eventManager = new EventManager();
	}

	private void initTab() {
		treeTab = new JTabbedPane();
		treeTab.setInputMap(JScrollPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new InputMap());
		for (int i = 0; i < trees.length; ++i) {
			treeTab.addTab(LAYER_NAMES[i], trees[i].getScrollPane());
		}
		treeTab.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				treeTabChanged();
			}
		});

		panelTab = new JTabbedPane();
		panelTab.setInputMap(JScrollPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, new InputMap());
		for (int i = 0; i < panels.length; ++i) {
			panelTab.addTab(LAYER_NAMES[i], panels[i].getBackPanel());
		}
		panelTab.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				panelTabChanged();
			}
		});
	}

	private void initInfo() {
		infoPanel = new SpriteInfoPanel();
	}

	private void initMenu() {
		//menuBar
		menuBar = new JMenuBar();
		//menuFile
		menuFile = new JMenu("�ļ�");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);
		//menuFileNew
		menuFileNew = new JMenuItem("�½���ͼ", KeyEvent.VK_N);
		menuFileNew.setActionCommand("NEWMAP");
		menuFileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		menuFileNew.addActionListener(menuActionListener);
		menuFile.add(menuFileNew);
		//menuFileLoad
		menuFileLoad = new JMenuItem("���ص�ͼ", KeyEvent.VK_L);
		menuFileLoad.setActionCommand("LOADMAP");
		menuFileLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		menuFileLoad.addActionListener(menuActionListener);
		menuFile.add(menuFileLoad);
		//menuFileSave
		menuFileSave = new JMenuItem("����", KeyEvent.VK_S);
		menuFileSave.setActionCommand("SAVEMAP");
		menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		menuFileSave.addActionListener(menuActionListener);
		menuFile.add(menuFileSave);
		//menuFileSavePNG
		menuFileSavePNG = new JMenuItem("����ΪPNG", KeyEvent.VK_P);
		menuFileSavePNG.setActionCommand("SAVEPNG");
		menuFileSavePNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		menuFileSavePNG.addActionListener(menuActionListener);
		menuFile.add(menuFileSavePNG);
		
		//menuEdit
		menuEdit = new JMenu("�༭");
		menuEdit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(menuEdit);
		//menuEditMapSize
		menuEditMapSize = new JMenuItem("�ı��ͼ��С", KeyEvent.VK_M);
		menuEditMapSize.setActionCommand("MAPPROP");
		menuEditMapSize.addActionListener(menuActionListener);
		menuEdit.add(menuEditMapSize);
		//menuEditFlipHorizontal
		menuEditFlipHorizontal = new JMenuItem("ˮƽ��ת", KeyEvent.VK_H);
		menuEditFlipHorizontal.setActionCommand("FLIPHORIZONTAL");
		menuEditFlipHorizontal.addActionListener(menuActionListener);
		menuEdit.add(menuEditFlipHorizontal);
		//menuEditFlipVertical
		menuEditFlipVertical = new JMenuItem("��ֱ��ת", KeyEvent.VK_V);
		menuEditFlipVertical.setActionCommand("FLIPVERTICAL");
		menuEditFlipVertical.addActionListener(menuActionListener);
		menuEdit.add(menuEditFlipVertical);
		//menuManager
		menuManager = new JMenu("����");
		menuManager.setMnemonic(KeyEvent.VK_M);
		menuBar.add(menuManager);
		//menuEventManager
		menuEventManager = new JMenuItem("�¼�����", KeyEvent.VK_E);
		menuEventManager.setActionCommand("EVENTMANAGER");
		menuEventManager.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		menuEventManager.addActionListener(menuActionListener);
		menuManager.add(menuEventManager);
		//menuItemManager
		menuItemManager = new JMenuItem("���ݿ�", KeyEvent.VK_I);
		menuItemManager.setActionCommand("ITEMMANAGER");
		menuItemManager.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
		menuItemManager.addActionListener(menuActionListener);
		menuManager.add(menuItemManager);
		//menuResetUnitMoveMode
		JMenuItem menuResetUnitMoveMode = new JMenuItem("����������ս��λ�ƶ�ģʽ������");
		menuResetUnitMoveMode.setActionCommand("RESETUNITMOVEMODE");
		menuResetUnitMoveMode.addActionListener(menuActionListener);
		menuManager.add(menuResetUnitMoveMode);
		//menuResetUnitFireMode
		JMenuItem menuResetUnitFireMode = new JMenuItem("����������ս��λ����ģʽ������");
		menuResetUnitFireMode.setActionCommand("RESETUNITFIREMODE");
		menuResetUnitFireMode.addActionListener(menuActionListener);
		menuManager.add(menuResetUnitFireMode);
		//menuResetUnitDIM
		JMenuItem menuResetUnitDIM = new JMenuItem("����������ս��λ������ߵ�����");
		menuResetUnitDIM.setActionCommand("RESETUNITDIM");
		menuResetUnitDIM.addActionListener(menuActionListener);
		menuManager.add(menuResetUnitDIM);
		//menuResetBuildingDIM
		JMenuItem menuResetBuildingDIM = new JMenuItem("�������н���������ߵ�����");
		menuResetBuildingDIM.setActionCommand("RESETBUILDINGDIM");
		menuResetBuildingDIM.addActionListener(menuActionListener);
		menuManager.add(menuResetBuildingDIM);
		//test
		JMenuItem menuTest = new JMenuItem("����", KeyEvent.VK_T);
		menuTest.setActionCommand("TEST");
		menuTest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
		menuTest.addActionListener(menuActionListener);
		menuManager.add(menuTest);
	}

	private void initToolBar() {
		//toolbar
		toolBar = new JToolBar();
		toolBar.setPreferredSize(new Dimension(100, XUtil.getDefPropInt("ToolBarHeight")));
		toolBar.setFloatable(false);
		//toolFlipHorizontal
		toolFlip = new JButton("F");
		toolFlip.setActionCommand("FLIP");
		toolFlip.addActionListener(menuActionListener);
		toolBar.add(toolFlip);
		toolBar.addSeparator();
		//toolRanges
		toolRanges = new RangeButton[XUtil.getDefPropInt("RangeCount")];
		for (int i = 0; i < toolRanges.length; ++i) {
			toolRanges[i] = new RangeButton(i);
			toolBar.add(toolRanges[i]);
		}
		//ButtonGroup
		ButtonGroup bp = new ButtonGroup();
		for (int i = 0; i < toolRanges.length; ++i) {
			bp.add(toolRanges[i]);
		}
		bp.setSelected(toolRanges[0].getModel(), true);
		toolBar.addSeparator();
		//toolShowAlpha
		toolShowAlpha = new JToggleButton("A", false);
		toolShowAlpha.setActionCommand("SHOWALPHA");
		toolShowAlpha.addActionListener(menuActionListener);
		toolShowAlpha.setSelected(true);
		toolBar.add(toolShowAlpha);
		//toolAlphaValue
		toolAlphaValue = new JSlider(0, 100, 0);
		toolAlphaValue.setMaximumSize(new Dimension(XUtil.getDefPropInt("ToolAlphaValueWidth"), 25));
		toolAlphaValue.setMajorTickSpacing(10);
		toolAlphaValue.setPaintTicks(true);
		toolAlphaValue.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				alphaValueChanged();
			}
		});
		toolAlphaValue.setValue(50);
		toolBar.add(toolAlphaValue);
		toolBar.addSeparator();
		//toolCoverable & toolUncoverable
		toolCoverable = new JButton("���ڵ�");
		toolCoverable.setActionCommand("COVERABLE");
		toolCoverable.addActionListener(menuActionListener);
		toolCoverable.setEnabled(false);
		toolBar.add(toolCoverable);
		toolUncoverable = new JButton("�����ڵ�");
		toolUncoverable.setActionCommand("UNCOVERABLE");
		toolUncoverable.addActionListener(menuActionListener);
		toolUncoverable.setEnabled(false);
		toolBar.add(toolUncoverable);
	}

	public MapInfo getMapInfo() {
		return mapInfo;
	}

	public Object[] getResManagers() {
		return resManagers;
	}

	public SpriteManagerPanel[] getPanels() {
		return panels;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	private class MenuActionListener
		implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String menuName = (e.getActionCommand()).trim().toUpperCase();
			if (menuName.equals("NEWMAP")) {
				newMap();
			}
			else if (menuName.equals("LOADMAP")) {
				loadMap();
			}
			else if (menuName.equals("SAVEMAP")) {
				saveMap();
			}
			else if(menuName.equals("SAVEPNG")) {
				savePNG();
			}
			else if (menuName.equals("MAPPROP")) {
				setMapProp();
			}
			else if (menuName.equals("FLIP")) {
				flip();
			}
			else if (menuName.equals("SHOWALPHA")) {
				showAlphaChanged();
			}
			else if (menuName.equals("EVENTMANAGER")) {
				manageEvents();
			}
//			else if(menuName.equals("ITEMMANAGER")) showItemManager();
//			else if(menuName.equals("COVERABLE")) setCoverable(true);
//			else if(menuName.equals("UNCOVERABLE")) setCoverable(false);
			else if (menuName.equals("RESETUNITMOVEMODE")) {
				resetUnitMoveMode();
			}
			else if (menuName.equals("RESETUNITFIREMODE")) {
				resetUnitFireMode();
			}
			else if (menuName.equals("RESETUNITDIM")) {
				resetUnitDIM();
			}
			else if (menuName.equals("RESETBUILDINGDIM")) {
				resetBuildingDIM();
			}
			else if (menuName.equals("TEST")) {
				test();
			}
		}
	}
	
	private void resetUnitMoveMode() {
		URManager urManager = (URManager)(resManagers[RES_UR]);
		UnitPanel unitPanel = (UnitPanel)(panels[LAYER_UNIT]);
		UnitManager unitManager = (UnitManager)(unitPanel.getManager());
		UnitRes[] urs = urManager.getURs();
		Sprite[] sprites = unitManager.getSprites();
		for(int i = 0; i < sprites.length; ++i) {
			if(sprites[i] == null) {
				continue;
			}
			if(!(sprites[i] instanceof Unit)) {
				continue;
			}
			Unit unit = (Unit)sprites[i];
			for(int j = 0; j < urs.length; ++j) {
				UnitRes ur = urs[j];
				if(ur.getID() == unit.getUR().getID()) {
					unit.setUM(ur.getUM());
					break;
				}
			}
		}
	}

	private void resetUnitFireMode() {
		URManager urManager = (URManager)(resManagers[RES_UR]);
		UnitPanel unitPanel = (UnitPanel)(panels[LAYER_UNIT]);
		UnitManager unitManager = (UnitManager)(unitPanel.getManager());
		UnitRes[] urs = urManager.getURs();
		Sprite[] sprites = unitManager.getSprites();
		for(int i = 0; i < sprites.length; ++i) {
			if(sprites[i] == null) {
				continue;
			}
			if(!(sprites[i] instanceof Unit)) {
				continue;
			}
			Unit unit = (Unit)sprites[i];
			for(int j = 0; j < urs.length; ++j) {
				UnitRes ur = urs[j];
				if(ur.getID() == unit.getUR().getID()) {
					unit.setUF(ur.getUF());
					break;
				}
			}
		}
	}
	
	private void resetUnitDIM() {
		URManager urManager = (URManager)(resManagers[RES_UR]);
		UnitPanel unitPanel = (UnitPanel)(panels[LAYER_UNIT]);
		UnitManager unitManager = (UnitManager)(unitPanel.getManager());
		UnitRes[] urs = urManager.getURs();
		Sprite[] sprites = unitManager.getSprites();
		for(int i = 0; i < sprites.length; ++i) {
			if(sprites[i] == null) {
				continue;
			}
			if(!(sprites[i] instanceof Unit)) {
				continue;
			}
			Unit unit = (Unit)sprites[i];
			for(int j = 0; j < urs.length; ++j) {
				UnitRes ur = urs[j];
				if(ur.getID() == unit.getUR().getID()) {
					unit.setDIM(ur.getDIM());
					break;
				}
			}
		}
	}
	
	private void resetBuildingDIM() {
		BRManager brManager = (BRManager)(resManagers[RES_BR]);
		BuildingPanel buildingPanel = (BuildingPanel)(panels[LAYER_BUILDING]);
		BuildingManager buildingManager = (BuildingManager)(buildingPanel.getManager());
		BuildingRes[] brs = brManager.getBRs();
		Sprite[] sprites = buildingManager.getSprites();
		for(int i = 0; i < sprites.length; ++i) {
			if(sprites[i] == null) {
				continue;
			}
			if(!(sprites[i] instanceof Building)) {
				continue;
			}
			Building building = (Building)sprites[i];
			for(int j = 0; j < brs.length; ++j) {
				BuildingRes br = brs[j];
				if(br.getID() == building.getBR().getID()) {
					building.setDIM(br.getDIM());
					break;
				}
			}
		}
	}

	private void treeTabChanged() {
		int index = treeTab.getSelectedIndex();
		if (panelTab.getSelectedIndex() != index) {
			panelTab.setSelectedIndex(index);
		}
		tabChanged();
	}

	private void panelTabChanged() {
		int index = panelTab.getSelectedIndex();
		if (treeTab.getSelectedIndex() != index) {
			treeTab.setSelectedIndex(index);
		}
		tabChanged();
	}

	private void tabChanged() {
		mouseInfo.resetAll();
		int index = panelTab.getSelectedIndex();
		infoPanel.setScrollablePanel(panels[index]);
		infoPanel.setSpriteManager(panels[index].getManager());
	}

	private void flip() {
		mouseInfo.setFlip(!mouseInfo.isFlip());
		SpriteManager manager = panels[panelTab.getSelectedIndex()].getManager();
		manager.flip();
	}

	private void showAlphaChanged() {
		for (int i = 0; i < panels.length; ++i) {
			panels[i].setShowAlpha(toolShowAlpha.isSelected());
		}
	}

	private void alphaValueChanged() {
		for (int i = 0; i < panels.length; ++i) {
			panels[i].setAlpha(toolAlphaValue.getValue() * 1.0 / toolAlphaValue.getMaximum());
		}
	}
	
	private void buildingTreeMouseClicked(MouseEvent e) {
		if (e.getButton() == XUtil.LEFT_BUTTON && e.getClickCount() >= 2) {
			buildingTreeDBClicked();
		}
	}

	private void buildingTreeDBClicked() {
		PainterTree buildingTree = trees[LAYER_BUILDING];
		if (buildingTree.getModel().getChildCount(buildingTree.getModel().getRoot()) <= 0) {
			setBRDB(null);
		}
		if (mouseInfo.getPainter() != null) {
			if (mouseInfo.getPainter()instanceof BRPainter) {
				setBRDB( ( (BRPainter) (mouseInfo.getPainter())).getBR());
			}
		}
	}

	private void setBRDB(BuildingRes br) {
		SIManager siManager = (SIManager) (resManagers[RES_SI]);
		ARManager arManager = (ARManager) (resManagers[RES_AR]);
		BRManager brManager = (BRManager) (resManagers[RES_BR]);

		BRDBSetter setter = new BRDBSetter(this, brManager, arManager, siManager);
		setter.setSelectedBR(br);
		setter.show();
		if (setter.getCloseType() == OKCancelDialog.OK_PERFORMED) {
			refreshBR();
		}
	}

	private void refreshBR() {
		BRManager brManager = (BRManager) (resManagers[RES_BR]);
		PainterTree buildingTree = trees[LAYER_BUILDING];
		BuildingPanel buildingPanel = (BuildingPanel) (panels[LAYER_BUILDING]);

		mouseInfo.resetAll();
		buildingTree.refresh(null, BRPainter.getPainters(brManager.getBRs()));
		( (BuildingManager) (buildingPanel.getManager())).refresh(brManager);
	}

	private void unitTreeMouseClicked(MouseEvent e) {
		if (e.getButton() == XUtil.LEFT_BUTTON && e.getClickCount() >= 2) {
			unitTreeDBClicked();
		}
	}

	private void unitTreeDBClicked() {
		PainterTree unitTree = trees[LAYER_UNIT];
		if (unitTree.getModel().getChildCount(unitTree.getModel().getRoot()) <= 0) {
			setURDB(null);
		}
		if (mouseInfo.getPainter() != null) {
			if (mouseInfo.getPainter()instanceof URPainter) {
				setURDB( ( (URPainter) (mouseInfo.getPainter())).getUR());
			}
		}
	}

	private void setURDB(UnitRes ur) {
		ARManager arManager = (ARManager) (resManagers[RES_AR]);
		URManager urManager = (URManager) (resManagers[RES_UR]);

		URDBSetter setter = new URDBSetter(this, urManager, arManager);
		setter.setSelectedUR(ur);
		setter.show();
		if (setter.getCloseType() == OKCancelDialog.OK_PERFORMED) {
			refreshUR();
		}
	}

	private void refreshUR() {
		URManager urManager = (URManager) (resManagers[RES_UR]);
		PainterTree unitTree = trees[LAYER_UNIT];
		UnitPanel unitPanel = (UnitPanel) (panels[LAYER_UNIT]);

		mouseInfo.resetAll();
		unitTree.refresh(null, URPainter.getPainters(urManager.getURs()));
		( (UnitManager) (unitPanel.getManager())).refresh(urManager);
	}

	private void newMap() {
		NewMapDialog setter = new NewMapDialog(this);
		setter.show();
		if(setter.getCloseType() == OKCancelDialog.OK_PERFORMED) {
			setter.updateMapInfo(mapInfo);
			for(int i = 0; i < panels.length; ++i) {
				panels[i].reset(mapInfo.getWidth(), mapInfo.getHeight());
			}
//			refreshBR();
//			refreshUR();
			setMapName(mapInfo.getName());
		}
	}
	
	private void setMapProp() {
		NewMapDialog setter = new NewMapDialog(this);
		setter.setTitle("���õ�ͼ����");
		setter.setMapInfo(mapInfo);
		setter.show();
		if (setter.getCloseType() == OKCancelDialog.OK_PERFORMED) {
			setter.updateMapInfo(mapInfo);
			for (int i = 0; i < panels.length; ++i) {
				panels[i].reset(mapInfo.getWidth(), mapInfo.getHeight());
			}
			setMapName(mapInfo.getName());
		}
	}
	
	private void setMapName(String name) {
		setTitle("��ͼ�༭��    -    " + name);
	}
	
	private void dealBeforeShowProgress() {
//    	setEnabled(false);
		progress.setTitle("����");
		progress.setInfo("��Ϣ");
		progress.setValue(0);
		progress.show();
	}

	private void dealAfterShowProgress() {
		try {
			synchronized(this) {
				this.wait(XUtil.getDefPropInt("progressWaitTime"));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		progress.hide();
		panelTab.repaint();
		validate();
		setEnabled(true);
		this.requestFocus();
	}

	private void saveMap() {
//		String mapName =
//			JOptionPane.showInputDialog(this, "�����ͼ���ƣ�",
//										"����", JOptionPane.INFORMATION_MESSAGE);
		String mapName = mapInfo.getName();
		if (mapName == null) {
			return;
		}
		mapName = mapName.trim();
		if (mapName.equals("")) {
			return;
		}
		
		SaveMapThread t = new SaveMapThread(mapName);
		t.start();
	}

	private void savePNG() {
		String mapName = mapInfo.getName();
		if (mapName == null) {
			return;
		}
		mapName = mapName.trim();
		if (mapName.equals("")) {
			return;
		}

		SavePNGThread t = new SavePNGThread(mapName);
		t.start();
	}
	
	private void loadMap() {
		String mapName =
			JOptionPane.showInputDialog(this, "�����ͼ���ƣ�",
										"����", JOptionPane.INFORMATION_MESSAGE);
		if (mapName == null) {
			return;
		}
		mapName = mapName.trim();
		if (mapName.equals("")) {
			return;
		}
		
		LoadMapThread t = new LoadMapThread(mapName);
		t.start();
	}

	private class SaveMapThread extends Thread {
		String mapName;
		public SaveMapThread(String mapName) {
			this.mapName = mapName;
		}

		public void run() {
			dealBeforeShowProgress();
			progress.setTitle("����");
			
			progress.setInfo("����֮ǰ�Ĵ浵�ļ�");
			backupSaveFiles(mapName);
			progress.setValue(50);
			
			try {
				saveMap(mapName);
			}
			catch (Exception e) {
				restoreSaveFiles(mapName);
				JOptionPane.showMessageDialog(progress, "���浱ǰ��ͼ����" + "\n" + e,
											  "�������", JOptionPane.ERROR_MESSAGE);
			}
			
			progress.setInfo("����ɹ�");
			progress.setValue(100);
			dealAfterShowProgress();
		}
	}
	
	private class SavePNGThread extends Thread {
		String mapName;
		public SavePNGThread(String mapName) {
			this.mapName = mapName;
		}

		public void run() {
			dealBeforeShowProgress();
			progress.setTitle("����ΪPNG");

			try {
				savePNG(mapName);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(progress, "����PNG����" + "\n" + e,
											  "�������", JOptionPane.ERROR_MESSAGE);
			}

			progress.setInfo("����ɹ�");
			progress.setValue(100);
			dealAfterShowProgress();
		}
	}

	private class LoadMapThread extends Thread {
		String mapName;
		public LoadMapThread(String mapName) {
			this.mapName = mapName;
		}

		public void run() {
			dealBeforeShowProgress();
			progress.setTitle("����");

			try {
			    loadMap(mapName);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(progress, "���ص�ͼ  " + mapName + "  ����" + "\n" + e,
											  "���س���", JOptionPane.ERROR_MESSAGE);
			}
			
			progress.setInfo("���سɹ�");
			progress.setValue(100);
			dealAfterShowProgress();
		}
	}

	private void saveMap(String name) throws Exception {		
		progress.setInfo("���������Ϣ");
		mapInfo.save();
		mapInfo.saveMobileData();
		progress.setValue(55);

		progress.setInfo("������ս��λ��Դ");
		( (BRManager) (resManagers[RES_BR])).save(name);
		( (BRManager) (resManagers[RES_BR])).saveMobileData(name);
		progress.setValue(60);
		
		progress.setInfo("���潨����Դ");
		( (URManager) (resManagers[RES_UR])).save(name);
		( (URManager) (resManagers[RES_UR])).saveMobileData(name);
		progress.setValue(65);
		
		for (int i = 0; i < panels.length; ++i) {
			if(i < LAYER_NAMES.length) {
			    progress.setInfo("����" + LAYER_NAMES[i]);
			}
			panels[i].save(name, resManagers);
			panels[i].saveMobileData(name, resManagers);
			progress.setValue((int)(65 + 1.0 * (90 - 65) * (i + 1) / panels.length));
		}
		
		progress.setInfo("�����¼�");
		eventManager.save(name);
		eventManager.saveMobileData(name);
		progress.setValue(100);
	}
	
	private void savePNG(String name) throws Exception {
		progress.setInfo("������ͼ");
		BufferedImage img = new BufferedImage(mapInfo.getWidth(),
											  mapInfo.getHeight(), 
											  BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		progress.setValue(10);
		
		for(int i = 0; i <= LAYER_UNIT; ++i) {
			if(i < LAYER_NAMES.length) {
				progress.setInfo("����" + LAYER_NAMES[i]);
			}
			if(i < panels.length) {
				panels[i].getManager().paintStatic(g);
				progress.setValue((int)(10 + 1.0 * (50 - 10) * (i + 1) / (LAYER_UNIT + 1)));
			}
		}
		
		progress.setInfo("����PNG�ļ�");
		img.flush();
		File f = new File(".\\save\\" + name + ".png");
		ImageIO.write(img, "png", f);
		progress.setValue(100);
	}

	private void loadMap(String name) throws Exception {
//		( (BRManager) (resManagers[RES_BR])).load(name, resManagers);
//		refreshBR();
		
//		( (URManager) (resManagers[RES_UR])).load(name, (ARManager) (resManagers[RES_AR]));
//		refreshUR();
		
		progress.setInfo("���ػ�����Ϣ");
		mapInfo.load(name);
		for(int i = 0; i < panels.length; ++i) {
			panels[i].reset(mapInfo.getWidth(), mapInfo.getHeight());
		}
//		((FloorManager)(panels[LAYER_FLOOR].getManager())).setColor(mapInfo.getColor());
		progress.setValue(20);
		
		for (int i = 0; i < panels.length; ++i) {
			if(i < LAYER_NAMES.length) {
				progress.setInfo("����" + LAYER_NAMES[i]);
			}
			panels[i].load(name, resManagers);
			progress.setValue((int)(20 + 1.0 * (80 - 20) * (i + 1) / panels.length));
		}
		
		progress.setInfo("�����¼�");
		eventManager.load(name);
		progress.setValue(100);
		setTitle("MB3Editor��" + name);
	}

	private final static String[][] SAVE_FILES = {
		{"BRFilePath", "BuildingRes.dat"}, 
		{"URFilePath", "UnitRes.dat"}, 
		{"MapInfoFilePath", "<��ͼ��>.dat"}, 
		{"FloorFilePath", "<��ͼ��>_Floor.dat"}, 
		{"TreeFilePath", "<��ͼ��>_Tree.dat"}, 
		{"BuildingFilePath", "<��ͼ��>_Building.dat"}, 
		{"UnitFilePath", "<��ͼ��>_Unit.dat"}, 
		{"WAFilePath", "<��ͼ��>_WA.dat"}, 
		{"RectFilePath", "<��ͼ��>_Rect.dat"}, 
		{"EventFilePath", "<��ͼ��>_Event.dat"}, 
		{"StringFilePath", "<��ͼ��>_String.txt"}, 
		{"SwitchFilePath", "<��ͼ��>_Switch.dat"},
		{"CounterFilePath", "<��ͼ��>_Counter.dat"}
	};

	private void backupSaveFiles(String name) {
		for(int i = 0; i < SAVE_FILES.length; ++i) {
			String filePath = XUtil.getDefPropStr(SAVE_FILES[i][0]);
			String backupPath = XUtil.getDefPropStr("BackupFilePath");
			String fileName = SAVE_FILES[i][1].replaceAll("<��ͼ��>", name);
			XUtil.copyFile(filePath + "\\" + fileName, backupPath + "\\" + fileName);
		}
	}

	private void restoreSaveFiles(String name) {
		for(int i = 0; i < SAVE_FILES.length; ++i) {
			String filePath = XUtil.getDefPropStr(SAVE_FILES[i][0]);
			String backupPath = XUtil.getDefPropStr("BackupFilePath");
			String fileName = SAVE_FILES[i][1].replaceAll("<��ͼ��>", name);
			XUtil.copyFile(backupPath + "\\" + fileName, filePath + "\\" + fileName);
		}
	}

	private class EventDBSetter
		extends OKCancelDialog {

		private MainFrame mainFrame;
		private EventList eventList;
		private Event[] events;

		public EventDBSetter(MainFrame mainFrame) {
			super(mainFrame);
			this.mainFrame = mainFrame;
			setTitle("�¼�����");
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			Container cp = this.getContentPane();
			events = mainFrame.getEventManager().getEvents();
			eventList = new EventList(events, this, mainFrame);
			cp.add(new JScrollPane(eventList), BorderLayout.CENTER);
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			cp.add(buttonPanel, BorderLayout.SOUTH);
			setLocationRelativeTo(mainFrame);
		}

		protected void okPerformed() {
			mainFrame.getEventManager().replace(events, eventList.getEvents());
			closeType = OK_PERFORMED;
			dispose();
		}

		protected void cancelPerformed() {
			dispose();
		}
	}

	private void manageEvents() {
		EventDBSetter setter = new EventDBSetter(this);
		setter.show();
	}

	private void test() {
//		UnitPath up = null;
//		UnitPathSetter setter = new UnitPathSetter(this, this, new IntPair(500, 500), up);
//		setter.show();
//		if (setter.getCloseType() == OKCancelDialog.OK_PERFORMED) {
//			up = new UnitPath(setter.getUnitPath());
//			System.out.println(UnitPath.getPathDescription(up.getPath()));
//		}

		JDialog tmp = new JDialog(this, true);
		tmp.setLocationRelativeTo(this);
		tmp.setSize(600, 500);
		Container cp = tmp.getContentPane();
		cp.setLayout(new BorderLayout());
		
		cp.add(new UnitMoveModePanel(tmp, new UnitMoveMode()));
		tmp.show();
//		BRExplorePanel p = new BRExplorePanel(tmp, (ARManager)resManagers[RES_AR]);
//		if(br == null) {
//			br = (HouseBR)(BuildingRes.createInstance(0, BuildingRes.HOUSE));
//			br.setImages(((SIManager)resManagers[RES_SI]).getSIs());
//		}
//		p.setBR(br);
//		cp.add(p.getBackPanel(), BorderLayout.CENTER);
//		cp.add(new MapAreaSetPanel(tmp, this, 0, 0, 0, 0), BorderLayout.CENTER);
//		tmp.show();
//		p.updateBRProp();
//		tmp.setLocationRelativeTo(this);
//		UnitPathMoveSetter setter = new UnitPathMoveSetter(tmp, this, um);
//		setter.show();
//		SISelecter s = new SISelecter(new JDialog(), 1, (SIManager)resManagers[RES_SI]);
//		s.show();
//		System.out.println(s.getSelectedSIID());
//		BRImageSelecter s = new BRImageSelecter(new JDialog(), br, (SIManager)resManagers[RES_SI]);
//		s.show();
//		BRDBSetter s = new BRDBSetter(this, (BRManager)(resManagers[RES_BR]), 
//									  (ARManager)(resManagers[RES_AR]), 
//									  (SIManager)(resManagers[RES_SI]));
//		s.show();
		
		
	}
}