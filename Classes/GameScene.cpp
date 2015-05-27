#include "GameScene.h"
#include "cocostudio/CocoStudio.h"
#include "ui/CocosGUI.h"
#include "GameLayer.h"
#include "Configure.h"
#include "GameOverLayer.h"
#include "EventManager.h"
#include "XMap.h"
#include "tool.h"
#include "CameraExt.h"
#include "Unit.h"
#include "MapManager.h"
#include "sharedptrobject.h"
#include "AnimationManager.h"
#include "AnimResManager.h"
#include "UnitResManager.h"
#include "buildingresmanager.h"
#include "weaponresmanager.h"
#include "UnitManager.h"
#include "buildingresmanager.h"
#include "consts.h"
#include "MyTime.h"
#include "Player.h"
#include "building.h"
#include "Bullets.h"

USING_NS_CC;

using namespace cocostudio::timeline;

GameScene::GameScene()
	:_state(EGS_SmallMap)
	, _nextState(EGS_SmallMap)
	, _stateStep(0)
	, _game1stStart(true)
	, _victory(false)
	, _firstRunSmallMap(true)
	, _gameEnd(false)
	, _eventQuick(false)
	, _eventLock(false)
	, _playerReckey(false)
	, _playerInCamara(false)
	, _pauseTick(0)
	, _pauseTickCount(0)
	, _life(1)
	, _playerDie(false)
	, _playerID(0)
	, _difficulty(0)
	, _stage(0)
	, _mapSectonID(0)
	, _bullets(0)
	, _playerDelay(0)
	, _waitToRun(0)
	, _gameEndTick(0)
	, _statTick(0)
	, _sortTick(0)
	, _collisionTick(0)
	, _spaceTick(0)
	, _deleteSpriteCount(0)
	, _gameOverPerfomTick(0)
	, _gameOverStepTick(0)
	, _gameOverColorDeep(0)
	, _chargeInde(0)
	, _startTime(0)
	, _psTime(0)
	, _totalTime(0)
	, _SMSType(0)
	, _usedBumpCount(0)
	, _dieLifes(0)
	, _scoreLevel(0)
	, _addScore(0)
	, _levelEnemyCount(0)
	, _killEnemyCount(0)
	, _killoddsF(0)
	, _cameraUnit(nullptr)
	, _player(nullptr)
	, _unitResManager(nullptr)
	, _buildingResManager(nullptr)
	, _weaponResManager(nullptr)
	, _AnimResManager(nullptr)
	, _unitManager(nullptr)
	, _eventManager(nullptr)
	, _activeMap(nullptr)
	, _mapManager(nullptr)
{

}

Scene* GameScene::createScene()
{
    // 'scene' is an autorelease object
    auto scene = Scene::create();
    
    // 'layer' is an autorelease object
    auto layer = GameScene::create();
	layer->setTag(LAYER_TAG_GAMESCENE);

    // add layer as a child to scene
    scene->addChild(layer, 2);

	//add waring layer;
	auto warningLayer = LayerColor::create(Color4B(255, 0, 0, 60));
	warningLayer->setOpacity(0);
	warningLayer->setTag(LAYER_TAG_WARNING);
	scene->addChild(warningLayer, 7);

    // return the scene
    return scene;
}

// on "init" you need to initialize your instance
bool GameScene::init()
{
    //////////////////////////////
    // 1. super init first
    if ( !Layer::init() )
    {
        return false;
    }
    //GameScene
	auto visibleSize = Director::getInstance()->getVisibleSize();
	auto origin = Director::getInstance()->getVisibleOrigin();
	/*auto sb = GameLayer::create();
	sb->setTag(LAYER_TAG_GAME);
	sb->setPosition(origin);
	addChild(sb);*/
	_text = Label::create();
	_text->setTextColor(Color4B::RED);
	_text->setString("level:");
	_text->setPosition(100, 100);
	addChild(_text);
#ifdef COCOS2D_DEBUG
	auto d = DrawNode::create();
	d->drawRect(Vec2(0, 0), Vec2(visibleSize.width, visibleSize.height), Color4F::RED);
	d->setPosition(origin);
	addChild(d);
#endif // 

	auto keyListner = EventListenerKeyboard::create();
	keyListner->onKeyReleased = [](EventKeyboard::KeyCode keyCode, Event* event){
		log("keyListner...%d", keyCode);
		switch (keyCode)
		{
		case EventKeyboard::KeyCode::KEY_ESCAPE:
			Director::getInstance()->end();
			break;
		default:
			break;
		}
	};
	_eventDispatcher->addEventListenerWithSceneGraphPriority(keyListner, this);
	//
	NotificationCenter::getInstance()->destroyInstance();
	NotificationCenter::getInstance()->addObserver(this, callfuncO_selector(GameScene::ShowGameOver), "ShowGameOver", nullptr);
	//
	_state = EGS_Init;
	_stateStep = 0;
	_firstRunSmallMap = false;

	scheduleUpdate();
	return true;
}

void GameScene::increaseScore(float dt)
{
	this->score++;
	std::stringstream ss;
	std::string str;
	ss << score;
	ss >> str;
	const char *p = str.c_str();
}

void GameScene::ShowGameOver(Ref* pObj)
{
	auto gameoverlayer = GameOverLayer::create(score);
	addChild(gameoverlayer, 10);
}

void GameScene::setNextGameState(int state)
{
	_nextState = state;
}

int GameScene::getGameState()
{
	return _state;
}

void GameScene::MoveCamera(int x, int y)
{
	if (_cameraUnit != nullptr && _camera->getType() >= 1 && _camera->getType() <= 2)
	{
		if (_cameraUnit->getMaxHP() <= 0)
		{
			_cameraUnit = nullptr;
			auto beforeType = _camera->getType();
			_camera->setType(0);
		}
		else
		{
			
		}
	}
}

std::shared_ptr<MapManager> GameScene::getMapManager()
{
	return _mapManager;
}

cocos2d::Size GameScene::getSceneSize()
{
	return Director::getInstance()->getVisibleSize();
}

float GameScene::getSceneHeight()
{
	return Director::getInstance()->getVisibleSize().height;
}

float GameScene::getSceneWidth()
{
	return Director::getInstance()->getVisibleSize().width;
}

void GameScene::loadEvent(int stage, int mapSectonID)
{
	char filename[256];
	sprintf(filename, "/mapdat/l%d_event_mobile.dat", MAPID_EVENT[stage][mapSectonID]);
	_eventManager = std::shared_ptr<EventManager>(new EventManager(this, filename));
}

void GameScene::MapWalkRectActive()
{
	auto size = Director::getInstance()->getVisibleSize();
	int i = 0;
	for (auto a : _activeMap->getWalkRect())
	{
		if (a.getMinY() > _camera->getY() + size.height ||
			a.getMaxX() < _camera->getY())
		{
			_activeMap->setWalkRectActive(i, false);
		}
		else
		{
			_activeMap->setWalkRectActive(i, true);
		}
		i++;
	}
}

void GameScene::spriteCollision()
{
	//�ۼƼ����������Ʋ�����Ҫ����ײ����
	if (_collisionTick >= 2)
	{
		_collisionTick = 0;
	}
	else
	{
		_collisionTick++;
	}
	//#ɾ���������ӵ�
	for (auto iter = UnitManager::_allyBullets.begin(); iter != UnitManager::_allyBullets.end();)
	{
		auto b = *iter;
		if (b->getCastoffStage() >= 1)
		{
			iter = UnitManager::_allyBullets.erase(iter);
		}
		else
		{
			++iter;
		}
	}
	for (auto iter = UnitManager::_enemyBullets.begin(); iter != UnitManager::_enemyBullets.end();)
	{
		auto b = *iter;
		if (b->getCastoffStage() >= 1)
		{
			iter = UnitManager::_enemyBullets.erase(iter);
		}
		else
		{
			++iter;
		}
	}
	//����player�������ײ����
	if (_collisionTick == 0)
	{
		for (auto a : UnitManager::_tools)
		{
			
			a->beTouch(_player);
		}
	}
	////��ը��ײ����
	explodeCollision();
	//�ӵ���ײ����
	bulletCollision();
}

void GameScene::explodeCollision()
{
	
}

void GameScene::bulletCollision()
{
	//�����˾��ӵ�
	for (auto b : UnitManager::_allyBullets)
	{
		if (b->isBump())
		{
			continue;
		}
		auto hitRect = b->getHitRect();
		auto power = b->getPower();
		//�˾��ӵ��ڵ�����ײ
		for (auto e : UnitManager::_enemies)
		{
			auto unit = dynamic_cast<Unit*>(e);
			if (unit && (unit->isActive() || unit->isCastoff()))
			{
				continue;
			}
			//�յ����������ӵ�����
			if (unit->beAttack(hitRect, power) != 0)
			{
				b->setBump();
				b->setCastoff();
			}
		}
		//�˾��ӵ��뽨����ײ
		if (b->isBump())
		{
			continue;
		}
		for (auto building : UnitManager::_buildings)
		{
			if (building->isActive())
			{
				if (building->beAttack(hitRect, power) != 0)
				{
					b->setBump();
					b->setCastoff();
					break;
				}
			}
		}
	}
	//���������ӵ�
	for (auto eb : UnitManager::_enemyBullets)
	{
		if (eb->isBump())
		{
			continue;
		}
		auto hitRect = eb->getHitRect();
		auto power = eb->getPower();
		//�����ӵ����˾�����
		for (auto a : UnitManager::_allys)
		{
			if (a->isCastoff())
			{
				continue;
			}
			if (a->beAttack(hitRect, power) != 0)
			{
				eb->setBump();
				eb->setCastoff();
				break;
			}
		}
		//�����ӵ��뽨����ײ
		if (eb->isBump())
		{
			continue;
		}
		if (_collisionTick == 1)
		{
			continue;
		}
		for (auto building : UnitManager::_buildings)
		{
			if (building->isActive() && 
				building->beAttack(hitRect, power) != 0)
			{
				eb->setBump();
				eb->setCastoff();
				break;
			}
		}
	}
}

void GameScene::gameInitPerform()
{
	switch (_stateStep)
	{
	case 0:
		if (_AnimResManager == nullptr)
		{
			/*_AnimResManager = new AnimResManager("");*/
		}
		break;
	case 1:
		if (_unitResManager == nullptr)
		{
			_unitResManager = std::shared_ptr<UnitResManager>(new UnitResManager("/mapdat/unitres_mobile.dat"));
		}
		if (_buildingResManager == nullptr)
		{
			_buildingResManager = std::shared_ptr<BuildingResManager>(new BuildingResManager("mapdat/buidingres_mobile.dat"));
		}

		break;
	case 2:
		if (_weaponResManager == nullptr)
		{
			_weaponResManager = std::shared_ptr<WeaponResManager>(new WeaponResManager());
		}
		_levelEnemyCount = 0;
		_unitManager = nullptr;
		_unitManager = std::shared_ptr<UnitManager>(new UnitManager(this));
		break;
	case 4:
		_mapManager = nullptr;
		_mapManager = std::shared_ptr<MapManager>(new MapManager(this));
		_camera = nullptr;
		_camera = std::shared_ptr<CameraExt>(new CameraExt(this, _mapManager));
		break;
	case 5:
		//�����ͼ���ݣ���һ��
		changeMap(MAPID_EVENT[_stage][_mapSectonID]);
		//�����ͼ���ݣ��ڶ���
		changeMap(MAPID_EVENT[_stage][_mapSectonID]);
		break;
	case 6:
		//�����ͼ���ݣ�������
		changeMap(MAPID_EVENT[_stage][_mapSectonID]);
		//�����ͼ���ݣ����Ĳ�
		changeMap(MAPID_EVENT[_stage][_mapSectonID]);
		break;
	case 7:
		//�����ͼ���ݣ����岽
		changeMap(MAPID_EVENT[_stage][_mapSectonID]);
		//�����ͼ���ݣ�������
		changeMap(MAPID_EVENT[_stage][_mapSectonID]);
		break;
	case 8:
		//�����ͼ���ݣ����߲�
		changeMap(MAPID_EVENT[_stage][_mapSectonID]);
		break;
	case 9:
		//�����ͼ���ݣ��ڰ˲�
		changeMap(MAPID_EVENT[_stage][_mapSectonID]);
		break;
	case 10:
		//�����ͼ���ݣ��ھŲ�
		changeMap(MAPID_EVENT[_stage][_mapSectonID]);
		break;
	case 11:
		if (!changeMap(MAPID_EVENT[_stage][_mapSectonID]))
		{
			return;
		}
		break;
	case 12:
		//todo�����޵�


		break;
	case 13:
		loadEvent(_stage, _mapSectonID);
		break;
	case 18:
		_gameEnd = false;
		_camera->setMoveRect(cocos2d::Rect(0, 0, _mapManager->getActiveMap()->getWidth(), _mapManager->getActiveMap()->getHeight()));
		_camera->close();
		_psTime = 0;
		_totalTime = 0;
		_usedBumpCount = 0;
		_dieLifes = 0;
		_scoreLevel = 0;
		_addScore = 0;
		_killEnemyCount = 0;
		_killoddsF = 0;
		if (_player != nullptr)
		{

		}
		//��¼��ʼʱ��
		_totalStartSecond = MyTime::getCurrentTime();
		_state = EGS_GamePlaying;
		_stateStep = 0;
		break;
	default:
		break;
	}
	log("%s,%d,%d", __FUNCTION__, _state, _stateStep);
	_stateStep++;
}

void GameScene::sortSprites()
{
	if (_sortTick > 2)
	{
		_sortTick = 0;
	}
	else
	{
		_sortTick++;
		if (_sortTick == 1)
		{
			for (auto a : UnitManager::_enemies)
			{
// 				if (a->getPower() > 0)
// 				{
// 
// 				}
			}
		}
	}
}

bool GameScene::changeMap(int mapID)
{
	bool changeOk = _mapManager->requestMap(mapID, true);
	if (changeOk)
	{
		_activeMap = _mapManager->getActiveMap();
	}
	return changeOk;
}
//ɾ��������ָ��
void GameScene::deleteCastoffPoint()
{
	for (auto iter = UnitManager::_buildings.begin();  iter != UnitManager::_buildings.end();)
	{
		auto building = *iter;
		if (building->isCastoff())
		{
			(*iter)->removeFromParent();
			iter = UnitManager::_buildings.erase(iter);
		}
		else
		{
			++iter;
		}
	}

	for (auto iter = UnitManager::_enemies.begin(); iter != UnitManager::_enemies.end();)
	{
		auto e = *iter;
		if (e->isCastoff())
		{
			log("delete %d", e->getUnitID());
			(*iter)->removeFromParent();
			iter = UnitManager::_enemies.erase(iter);
		}
		else
		{
			++iter;
		}
	}

	for (auto iter = UnitManager::_allys.begin(); iter != UnitManager::_allys.end();)
	{
		auto e = *iter;
		if (e->isCastoff())
		{
			(*iter)->removeFromParent();
			iter = UnitManager::_allys.erase(iter);
		}
		else
		{
			++iter;
		}
	}

	for (auto iter = UnitManager::_tools.begin(); iter != UnitManager::_tools.end();)
	{
		auto e = *iter;
		if (e->isCastoff())
		{
			(*iter)->removeFromParent();
			iter = UnitManager::_tools.erase(iter);
		}
		else
		{
			++iter;
		}
	}
}

void GameScene::gamePlayingPerform(float dt)
{
	log("enter GameScene::gamePlayingPerform....");

	//#�����¼�
	_eventManager->doPeriodicTask();
	if (_eventLock)
	{
		return;
	}
	//��������û�д򿪣����˳�
// 	if (_camera->isClose())
// 	{
// 		return;
// 	}
	//��ͼ���߷�Χ����
	MapWalkRectActive();
	for (auto iter = UnitManager::_sprites.begin(); iter != UnitManager::_sprites.end();)
	{
		if ((*iter)->getCastoffStage() > 1)
		{
			(*iter)->removeFromParent();
			log("delete sprite...");
			iter = UnitManager::_sprites.erase(iter);
		}
		else
		{
			(*iter)->perfrom(dt);
			++iter;
		}
	}
	//������ײ����
	/*spriteCollision();*/
	//ɾ���������ķ�������ֵ
	deleteCastoffPoint();
	//��Sprite����
	//sortSprites();
	//�����
	moveCamera(dt);
	if (_player != nullptr && _player->getPower() <= 0)
	{
		if (_life <= 0)
		{
			//#todo gameover
		}
	}
}

void GameScene::changeMapByEvent(int stage, int mapFileID)
{
	_totalScore += _scoreLevel;
	if (_state != stage)
	{
		_stage = stage;
		_mapSectonID = mapFileID;
		_playerPower = 0;
		_bullets = 0;
	}
	else
	{
		_stage = EGS_LoadMap;
		_stateStep = mapFileID;
		/*save();*/

	}
}

Unit* GameScene::getPlayer()
{
	return _player;
}

void GameScene::addLevelEnemy(int count)
{
	_levelEnemyCount += count;
}

void GameScene::addScore(int param1)
{
	_addScore += param1;
}

std::shared_ptr<CameraExt> GameScene::getCamera()
{
	return _camera;
}

std::shared_ptr<BuildingResManager> GameScene::getBuildingResManager()
{
	return _buildingResManager;
}

void GameScene::setPlayerPower(int power)
{
	_playerPower = power;
}

void GameScene::addKillEnemy(int count)
{
	_killEnemyCount += count;
}

void GameScene::setGameState(int state)
{
	_state = state;
}

void GameScene::cameraOperationOK(int type)
{
	switch (type)
	{
	case 6://�л���ĳ��
		refreshMap(_camera->getY(), true);
		break;
	default:
		break;
	}
}

void GameScene::refreshMap(float offY, bool move)
{
//#todo
}

void GameScene::startGameState()
{
}

void GameScene::startGameEnd()
{
}

void GameScene::startGameOver()
{
}

int GameScene::getMapWidth()
{
	return _mapManager->getXMapW();
}

int GameScene::getMapHeight()
{
	return _mapManager->getXMapH();
}

void GameScene::doPeroidicTick(float dt)
{
	_text->setString("level:" + _state);
	switch (_state)
	{
	case EGS_GamePlaying:
		gamePlayingPerform(dt);
		break;
	case EGS_LoadMap:
		gameLoadMapPerform();
		break;
	case EGS_Init:
		gameInitPerform();
		break;
	case EGS_Stat:
		switch (_stateStep)
		{
		case 0:
			_totalVicorySecond = MyTime::getCurrentTime();
			_totalUsedSecond = (_totalVicorySecond - _totalStartSecond) / 1000;
			_totalHour = _totalUsedSecond / 3600;
			_totalMinute = _totalUsedSecond / 60 - _totalHour * 60;
			_totalSecond = _totalUsedSecond - _totalUsedSecond * 3600 - _totalMinute * 60;
			_stateStep = 1;
			break;
		case 1:

			break;
		default:
			break;
		}
		break;
	default:
		break;
	}
}

void GameScene::gameLoadMapPerform()
{
	switch (_stateStep)
	{
	case 1:
		_levelEnemyCount = 0;
		_cameraUnit = nullptr;
		_unitManager = std::shared_ptr<UnitManager>(new UnitManager(this));
		break;
	case 2:
		if (!changeMap(MAPID_EVENT[_stage][_mapSectonID]))
		{
			return;
		}
		break;
	case 3:
		loadEvent(_state, _mapSectonID);
		break;
	case 4:
		_gameEnd = false;
		_camera->setMoveRect(cocos2d::Rect(0, 0, _mapManager->getActiveMap()->getWidth(), _mapManager->getActiveMap()->getHeight()));
		_camera->close();
		if (_player != nullptr)
		{
			unitFollowCamer(_player, true);
		}
		break;
	case 5:
		_state = EGS_GamePlaying;
		_stateStep = 0;
		//#����boss
		break;
	default:
		break;
	}
	_stateStep++;
}

void GameScene::unitFollowCamer(Unit* _player, bool center)
{
	
}

void GameScene::update(float delta)
{
	doPeroidicTick(delta);
}

UnitManager& GameScene::getUnitManager() const
{
	return *_unitManager;
}

void GameScene::setPlayer(Player* unit)
{
	_player = unit;
}

UnitResManager& GameScene::getUnitResManager() const
{
	return *_unitResManager;
}

int GameScene::getDefficulty()
{
	return _difficulty;
}

bool GameScene::getUnitCollisionToMap(cocos2d::Rect tempRect)
{
	for (auto i = 0; i < (int)_activeMap->getWalkRect().size(); i++)
	{
		if (!_activeMap->isWalkRectActive(i))
		{
			continue;
		}
		if (_activeMap->getWalkRect()[i].intersectsRect(tempRect))
		{
			return false;
		}
	}
	return true;
}
//unit������ײ����
bool GameScene::getUnitCollision(cocos2d::Rect tempRect)
{
	for (auto iter = UnitManager::_buildings.begin(); iter != UnitManager::_buildings.end(); ++iter)
	{
		auto building = *iter;
		if (building->isActive())
		{
			continue;
		}
		if (building->getWalkRect().size.width > 0 && tempRect.intersectsRect(building->getWalkRect()))
		{
			return false;
		}
	}
	return true;
}

void GameScene::moveCamera( float dt )
{
	if (_cameraUnit != nullptr && _camera->getType() >= 1 && _camera->getType() <= 2)
	{
		if (_cameraUnit->getPower() <= 0)
		{
			_cameraUnit = nullptr;
			_cameraBeforeType = _camera->getType();
			_camera->setType(0);
		}
		else
		{
			_camera->setDesX(_cameraUnit->getPositionX());
			_camera->setDesY(_cameraUnit->getPositionY());
		}
	}
	_camera->doPeriodicTask(dt);

	if (_cameraUnit && _cameraUnit->getType() == 2)//����player
	{
		auto tempX = _cameraUnit->getPositionX();
		auto tempY = _cameraUnit->getPositionY();
		if (tempX < _camera->getX() + 8)
		{
			tempX = _camera->getX() + 8;
		}
		else if (tempX > _camera->getX() + getSceneHeight() - 8)
		{
			tempX = _camera->getX() + getSceneHeight() - 8;
		}
		if (tempY < _camera->getY() + 16)
		{
			tempY = _camera->getY() + 16;
		}
		else if (tempY > _camera->getY() + getSceneHeight() - 20)
		{
			tempY = _camera->getY() + getSceneHeight() - 20;
		}
		/*_camera->SetCameraPos(tempX, tempY);*/
	}
	_mapManager->performMap();
}

cocos2d::Size GameScene::getMapSize()
{
	_mapManager->getActiveMap();
	return Size::ZERO;
}

GameScene* GameScene::create()
{
	auto ret = new GameScene();
	if (ret && ret->init())
	{
		ret->autorelease();
		return ret;
	}
	CC_SAFE_DELETE(ret);
	return ret;
}

void GameScene::addUnit(Node* node)
{
	if (_mapManager->getFloor()) _mapManager->getFloor()->addChild(node);
}

