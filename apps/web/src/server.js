var http = require('http');
var url = require('url');
var fs = require('fs');

const { SSMClient, GetParameterCommand } = require("@aws-sdk/client-ssm");

copyFile()
// replaceUrl("./main.js")
var server = http.createServer(function(request, response) {
    var path = url.parse(request.url).pathname;
    fs.readFile(__dirname + path, function(error, data) {
        if (error) {
            response.writeHead(404);
            console.log(error)
            //response.write(error);
            response.end();
        } else {
            contentType = 'text/html'
            if(path.endsWith('.js')){
                contentType = 'application/javascript'
            }
            response.writeHead(200, {
                'Content-Type': contentType
            });
            response.write(data);
            response.end();
        }
    });
});


function copyFile(){
    fs.copyFileSync('./main-template.js', './main.js');
}

async function replaceUrl(fileName){
    
    const client = new SSMClient({ region: "ap-southeast-1" });
    const command = new GetParameterCommand({"Name":"/ab3-demo/ab3-app-web/api-id"});
    const response = await client.send(command);
    console.log(response)
    apigw_url = response.Parameter.Value
    fs.readFile(fileName, 'utf8', function (err, data) {
        if (err) {
              return console.log(err);
        }
        var result = data.replace(/APIGWURL/g, apigw_url);
          
        fs.writeFile(fileName, result, 'utf8', function (err) {
            if (err) return console.log(err);
        });
    });
    
}

server.listen(8082);