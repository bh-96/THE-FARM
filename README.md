# 프로젝트 기술서

&nbsp;

&nbsp;

&nbsp;

### 1. 개요

- 프로젝트 명 : THE FARM
- 프로젝트 진행일 : 2018.03 ~ 2018.05
- 프로젝트 유형 : 팀 프로젝트 (3명)
- 주사용 기술 : Android Studio, Node.js, MySQL
- 프로젝트 설명 : 다마고치 기반의 작물 키우기 게임 (안드로이드 앱)

&nbsp;

&nbsp;

### 2. 기능

안드로이드 기반의 모바일 게임 개발 프로젝트로서, ‘다마고치’를 모티브 삼은 작물 키우기 게임입니다. 
밥 주기, 간식 주기, 쓰다듬기, 미니게임, 청소 등을 수행하며 캐릭터를 키워 경험치를 향상시키고 최종 레벨(3단계)까지 캐릭터를 키우면 성공하는 게임입니다.

(캐릭터 이미지 참조 : 넷마블 야채 부락리)

- Node.js 로 API 서버 구축
  - 데이터베이스 연동
- 로그인
  - 캐릭터 이름과 캐릭터의 상태로 게임 시작 여부 체크
- 캐릭터 생성
  - 캐릭터 이름, 캐릭터 선택
- 캐릭터 키우기 : 포만감, 행복, 건강, Active, 스트레스, 경험치
  - 밥 주기, 간식 주기 : 포만감, 행복, 건강 증가 / 스트레스 감소
  - 쓰다듬기 : 행복 증가 / 스트레스 감소
  - 청소 : 스레드를 통해 일정하게 쓰레기가 나타나면 쓰레기를 클릭하여 청소
  - 미니게임
    - 혼자하기
      - 1 to 50 : 일정 시간 내에 1부터 50까지 순서대로 선택
      - 카드 뒤집기 : 뒤집힌 카드에서 동일한 카드를 짝지어 선택
    - 같이하기
      - 다른 사용자의 캐릭터가 로그인되어 있고, 게임 중이 아닌 상태일 때 함께 게임하기 요청
      - 다른 사용자가 함께 게임하기 수락하면 만보기 게임 진행
      - 먼저 만보기 횟수를 채운 사용자가 승리
      - 승리한 사용자에게 메달 부여

&nbsp;

&nbsp;

### 3. 데이터베이스 테이블 구조

```sql
create table user
(
	num int null comment '캐릭터 종류',
	name varchar(50) not null comment '캐릭터 이름',
	wait int default 0 null comment '캐릭터 상태',
	hunger int null comment '포만감 상태',
	happiness int null comment '행복 상태',
	health int null comment '건강 상태',
	active int null comment 'Active 상태',
	stress int null comment '스트레스 상태',
	experience int null comment '경험치 상태',
	end_time varchar(20) null comment '종료 시간',
	medal int null comment '획득한 메달 개수',
	rst_to varchar(20) null comment '미니게임 같이하기 한 상대 캐릭터 이름',
	result varchar(10) null comment '미니게임 같이하기 우승 결과'
);
```

