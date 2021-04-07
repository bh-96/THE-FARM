const express = require('express');
const app = express();
var router = express.Router();
var mysql = require('mysql');

var num, user, other;
var hunger, happiness, health, active, stress, experience, rst, rst_to, medal;

// DATABASE SETTING
var connection = mysql.createConnection({
    host     : '114.70.234.153',
    port     : 3306,    
    user     : 'capstone',
    password : '1234',
    database : 'capstone'
});

connection.connect();

module.exports = router;

//MainActivity(wait=0)
app.post('/main', (req, res) => {
    var inputData;

    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
    });
   
    req.on('end', () => {
        connection.query("select * from user where name='" + user + "' and wait=0", function(err, rows, fields){
            var idx = rows.length - 1;
    
            if(!err){
                if(rows.length > 0){
                    res.end(rows[idx].hunger + "," + rows[idx].happiness + "," + rows[idx].health + "," + rows[idx].active + "," + rows[idx].stress + "," + rows[idx].experience+ "," + rows[idx].end_time);
                    console.log("Tycoon start > " + user);
                }else{
                    res.end("0");
                }
            }
        })
    });
});

//Delete
app.post('/delete', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
    });
     
    req.on('end', () => {
        connection.query("select * from user where name='" + user + "'", function(err, rows, fields){
            if(!err){
                if(rows.length > 0){
                    connection.query("delete from user where name='" + user + "'", function(err, rows, fields) {
                        if(err) { throw err; }     
                        console.log("delete character:" + user);
                    });
                    res.end("delete");   //캐릭터 삭제
                }else{ 
                    res.end("0");   //존재하지 않는 캐릭터 정보
                }
            }
        })
    });
});

//Section3
app.post('/sec3', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        num = inputData.num;
        user = inputData.name;
    });
     
    req.on('end', () => {
        connection.query("select * from user where name='" + user + "'", function(err, rows, fields){
            if(!err){
                if(rows.length > 0){
                    res.end("0");   //이미 존재하는 이름
                }else{                
                    connection.query("insert into user(num, name) values('" + num + "', '" + user + "')", function(err, rows, fields) {
                        if(err) { throw err; }     
                        console.log("insert character: crop" + num + ", " + user);
                    });
                    res.end("1");   //캐릭터 생성 -> Section4
                }
            }
        })
    });
});

//Section4(wait=1)
app.post('/sec4', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
    });
     
    req.on('end', () => {
        connection.query("select * from user where name='" + user + "'", function(err, rows, fields){
            var idx = rows.length - 1;    
            if(!err){
                if(rows.length > 0){
                    connection.query("update user set wait=1 where name='" + user + "'", function(err, rows, fields){
                                          
                    })  
                    res.end(rows[idx].num + "," + rows[idx].name);                  
                    console.log("Tycoon start > " + user);
                }
            }
        })
    });
});

//캐릭터 상태 저장
app.post('/save', (req, res) => {
    var inputData;

    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
        hunger = inputData.hunger;
        happiness = inputData.happiness;
        health = inputData.health;
        active = inputData.active;
        stress = inputData.stress;
        experience = inputData.experience;
        end_time = inputData.end_time
    });

    req.on('end', () => {
        connection.query("select * from user", function(err, rows, fields){
            if(!err){
                if(rows.length > 0){
                    connection.query("update user set hunger=" + hunger + ", happiness=" + happiness + ", health=" + health + ", active=" + active + ", stress=" + stress + ", experience=" + experience + ",end_time=" + end_time + ", wait=0 where name='" + user + "'", function(err, rows, fields) {
                        if(err) { throw err; }
                    });
                    res.end("save");
                    console.log("Save " + user + "'s state");
                }
            }
        })
    });
});


// ** 게임하기 부분 **

//game_menu(wait=2)
app.post('/game', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
    });
     
    req.on('end', () => {
        connection.query("select * from user", function(err, rows, fields){
            var idx = rows.length - 1;
    
            if(!err){
                if(rows.length > 0){
                    connection.query("update user set wait=2 where name='" + user + "'", function(err, rows, fields) {
                        if(err) { throw err; }     
                    });
                    res.end("game");
                    console.log("Game menu > " + user);
                }
            }
        })
    });
});

// ** 혼자놀기 **

//alone(wait=3)
app.post('/alone', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
    });
     
    req.on('end', () => {
        connection.query("select * from user", function(err, rows, fields){
            var idx = rows.length - 1;
    
            if(!err){
                if(rows.length > 0){
                    connection.query("update user set wait=3 where name='" + user + "'", function(err, rows, fields) {
                        if(err) { throw err; }     
                    });
                    res.end("alone");
                    console.log("Alone > " + user);
                }
            }
        })
    });
});

//result 1 to 50
app.post('/result', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
        rst = inputData.result;
    });
     
    req.on('end', () => {
        connection.query("select * from user", function(err, rows, fields){    
            if(!err){
                if(rows.length > 0){
                    connection.query("update user set result='" + rst + "' where name='" + user + "'", function(err, rows, fields) {
                        if(err) { throw err; }     
                    });
                    res.end("result");
                    console.log(user + "'s 1to50 result: " + rst);
                }
            }
        })
    });
});

// ** 같이놀기 **

//together(wait=4)
app.post('/together', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
    });
     
    req.on('end', () => {
        connection.query("select * from user", function(err, rows, fields){
            var idx = rows.length - 1;
    
            if(!err){
                if(rows.length > 0){
                    connection.query("update user set wait=4 where name='" + user + "'", function(err, rows, fields) {
                        if(err) { throw err; }     
                    });
                    res.end("together");
                    console.log("Game Together > " + user);
                }else{
                    res.end("fail");
                }
            }
        })
    });
});

//친구 이름이 user table에 있는지 확인
app.post('/other', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        other = inputData.other;
    });
     
    req.on('end', () => {
        connection.query("select * from user where name='" + other + "'", function(err, rows, fields){
            if(!err){
                if(rows.length > 0){
                    //찾는 친구 ID가 있을 때
                    res.end("other");  
                }else{                
                    //찾는 친구 ID가 없을 때
                    res.end("no");
                }
            }
        })
    });
});

//친구에게 같이 게임하자고 요청
app.post('/go', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        other = inputData.other;
    });
     
    req.on('end', () => {
        connection.query("select * from user where name='" + other + "' and wait=4", function(err, rows, fields){
            if(!err){
                if(rows.length > 0){
                    //찾는 친구 ID가 있을 때
                    res.end("go");  //만보기게임으로  
                }else{                
                    //찾는 친구 ID가 없을 때
                    res.end("waiting");   //toast(친구가 게임시작 버튼을 누르지 않음)
                }
            }
        })
    });
});

//만보기 게임 기록
app.post('/together_result', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
        rst_to = inputData.result;
    });
     
    req.on('end', () => {
        connection.query("select * from user where name='" + user + "' and wait=4", function(err, rows, fields){
            if(!err){
                if(rows.length > 0){
                    connection.query("update user set rst_to='" + rst_to + "' where name='" + user + "'", function(err, rows, fields) {
                        if(err) { throw err; }     
                    });
                    
                    res.end("ok");  //기록 성공 
                    console.log("같이놀기: " + user + "_" + rst_to);
                }else{                
                    res.end("fail");   //기록 실패
                }
            }
        })
    });
});

//만보기 게임 비교
app.post('/compare', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
        other = inputData.other;
    });
     
    req.on('end', () => {
        connection.query("select name from user where name in ('" + user + "', '" + other + "') order by rst_to asc",
         function(err, rows, fields){
            var idx = rows.length - 1;

            if(!err){
                if(rows.length > 0){
                    res.end(rows[idx].name); 
                    console.log("승자: " + rows[idx].name);
                }else{                
                    res.end("lose");
                }
            }
        })
    });
});


//메달 수 보내기
app.post('/medal', (req, res) => {
    var inputData;

    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
    });
   
    req.on('end', () => {
        connection.query("select * from user where name='" + user + "'", function(err, rows, fields){
            var idx = rows.length - 1;
    
            if(!err){
                if(rows.length > 0){
                    res.end("," + rows[idx].medal);
                    console.log(user + "'s medal: " + rows[idx].medal);
                }else{
                    res.end("fail");
                    console.log("nonono");
                }
            }
        })
    });
});

//만보기 게임에서 이기면 medal 증가
app.post('/save_medal', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.name;
        medal = inputData.medal;
    });
     
    req.on('end', () => {
        connection.query("select * from user where name='" + user + "'", function(err, rows, fields){
            if(!err){
                if(rows.length > 0){
                    connection.query("update user set medal=" + medal + ", rst_to=null where name='" + user + "'", function(err, rows, fields) {
                        if(err) { throw err; }     
                    });
                    
                    res.end("medal");  //기록 성공 
                    console.log(user + " > medal++");
                }else{                
                    res.end("fail");   //기록 실패
                    console.log(user + " > failed");
                }
            }
        })
    });
});

//친구의 게임기록을 기다림
app.post('/record', (req, res) => {
    var inputData;
  
    req.on('data', (data) => {
        inputData = JSON.parse(data);
        user = inputData.user;
        other = inputData.other;
    });
     
    req.on('end', () => {
        connection.query("select * from user where name='" + other + "' and rst_to is null", function(err, rows, fields){
            if(!err){
                if(rows.length > 0){                    
                    res.end("record");  //아직 친구의 게임기록이 DB에 저장되지 않음 
                    console.log("Wait!");
                }else{                
                    res.end("end");  
                    console.log("Game End!");
                }
            }
        })
    });
});


app.listen(3000, () => {
    console.log('Server Connection!');
});