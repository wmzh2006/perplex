#ifndef __shotlogic_h__
#define __shotlogic_h__

#include "cocos2d.h"
#include "mathconst.h"

class Unit;

class ShotLogic
{
public:
	
public:
	ShotLogic(Unit* unit, int weaponResID, int posIndex);
	virtual ~ShotLogic();
	virtual void perform();;
	bool isEnd();
protected:
	bool _end;
	short _firetick;
	short _performTick;
	int _posIndex;
	int _weaponResID;
	int _speed;
	Unit* _unit;
};
//////////////////////////////////////////////////////////////////////////
//Բ����
// //////////////////////////////////////////////////////////////////////////

static const float LOGICASPEED = 3.0f;

class ShotLogicA : public ShotLogic
{
public:
	ShotLogicA(Unit* unit, int weaponResID, int posIndex);

	void perform();
protected:
	short _delay;
	int _shotDirection;
	float _fireAngle; 
};

//////////////////////////////////////////////////////////////////////////
//ֱ��׷����
//////////////////////////////////////////////////////////////////////////

class ShotLogicB : public ShotLogic
{
public:
	ShotLogicB(Unit* unit, int weaponResID, int posIndex, int shotCount);

	~ShotLogicB();

	void perform();
private:
	short _delay;
	int _fireCount;
	int _shotCount;
	Unit* _goalUnit;
};

//////////////////////////////////////////////////////////////////////////
//������ָ������
//////////////////////////////////////////////////////////////////////////

class ShotLogicC : public ShotLogic
{
public:
	ShotLogicC(Unit* unit, int weaponResID, int posIndex);

	void perform();
};

//////////////////////////////////////////////////////////////////////////
//��������
//////////////////////////////////////////////////////////////////////////

class ShotLogicD : public ShotLogic
{
public:
	static const int LOGIC_D_MAXFIRECOUNT = 1;
public:
	ShotLogicD(Unit* unit, int weaponResID, int posIndex);

	void perform();
private:
	int _fireCount;
	short _delay;
};


//////////////////////////////////////////////////////////////////////////
//˫�浯
//////////////////////////////////////////////////////////////////////////

class ShotLogicE : public ShotLogic
{
public:
	ShotLogicE(Unit* unit, int weaponResID, int posIndex, bool dir);
	~ShotLogicE();

	void perform();
private:
	bool _dir;
	Unit* _goalUnit;
};


//////////////////////////////////////////////////////////////////////////
//���浯����浥����׼player��
//////////////////////////////////////////////////////////////////////////

class ShotLogicF : public ShotLogic
{
public:
	ShotLogicF(Unit* unit, int weaponResID, int posIndex, int shotCount, bool dir);
	~ShotLogicF();

	void perform();
private:
	bool _dir;
	int _shotCount;
	Unit* _goalUnit;
};

//////////////////////////////////////////////////////////////////////////
//˫��ָ������
//////////////////////////////////////////////////////////////////////////
class ShotLogicG : public ShotLogic
{
public:
	

private:

};

//////////////////////////////////////////////////////////////////////////
//�˷���ͬʱ����
//////////////////////////////////////////////////////////////////////////
class ShotLogicH : public ShotLogic
{
public:
	ShotLogicH(Unit* unit, int weaponResID, int posIndex);
	
	void perform();
};

//////////////////////////////////////////////////////////////////////////
//16����ͬʱ����
//////////////////////////////////////////////////////////////////////////
class ShotLogicI : public ShotLogic
{
public:
	ShotLogicI(Unit* unit, int weaponResID, int posIndex);

	void perform();
};
//////////////////////////////////////////////////////////////////////////
//������
//////////////////////////////////////////////////////////////////////////
class ShotLogicK : public ShotLogic
{
public:
	ShotLogicK(Unit* unit, int weaponResID, int posIndex);

	void perform();
};

//////////////////////////////////////////////////////////////////////////
//����̹����С��
//////////////////////////////////////////////////////////////////////////
class ShotLogicQ : public ShotLogic
{
public:
	ShotLogicQ(Unit* unit, int weaponResID, int posIndex, int shotCount);

	~ShotLogicQ();
	void perform();
private:
	short _delay;
	int _fireCount;
	int _shotCount;
	Unit* _goalUnit;
};
#endif // __shotlogic_h__