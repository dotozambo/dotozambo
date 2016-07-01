dotozambo
=========

This Project is My Amazone Web Service Project
----------------------------------------------
#### 1. Installed
> 
* JDK 1.8 Upper
* STS and Gradle Support plugin
* Git
* Docker and boot2docker

#### 2. Setting

> 
* AWS Setting
	* EC2 Port Setting
	: http://docs.aws.amazon.com/ko_kr/AWSEC2/latest/UserGuide/get-set-up-for-amazon-ec2.html
	* LB HTTPS Certification Setting
	: http://docs.aws.amazon.com/ko_kr/IAM/latest/UserGuide/id_credentials_server-certs_create.html
	* Server Timezone Setting
	: http://hotpotato.tistory.com/31

> 
* Docker Install in EC2 System
	* Spring-boot Sample Test
	: https://spring.io/guides/gs/spring-boot-docker/
			
> 
* DNS Setting (Route 53)
	* EC2 Domain Setting
	: http://wingsnote.com/57
	* ELB HTTPS Domain Setting
	: http://wildpup.cafe24.com/archives/867
	
> 
* Postfix Mail Server Setting
: https://help.ubuntu.com/community/PostfixBasicSetupHowto
: https://kldp.org/node/136332
: http://fsteam.tistory.com/67
	
> * ETC
: https://treewiki.s3.amazonaws.com/docker/springboot-aws-docker.html
: https://brunch.co.kr/@brunchqvxt/1
: http://seongtak-yoon.tistory.com/10 - Spring-boot HTTPS Local Setting

### 3. Release Command Synopsis

>
    ########################### Local ###########################
    user@local$ [git pull]
    user@local$  git add "Source Path"
    user@local$  git commit -m "Commit Messages"
    user@local$  git push
    ################### Local - Docker shell ####################
    user@local-docker-shell$  cd "Source Directory" - at pom.xml
    user@local-docker-shell$  mvn clean package [-P real] [-Dmaven.test.skip=true] docker:build
    user@local-docker-shell$ [docker login]
    user@local-docker-shell$  docker push "image/tag"
    ######################## EC2 - shell #########################
    ec2-user@shell$ [docker login]
    ec2-user@shell$  docker pull "image/"tag"
    ec2-user@shell$ [docker kill "container ID"
    ec2-user@shell$  docker run [--env arg1=ARG1 --env arg2=ARG2] -p "port:port" -t "image/tag"
    
	
### 4. Ref
> 
* Docker repo - dotozambo/dotozambo
* Git Menual - https://rogerdudler.github.io/git-guide/index.ko.html
* Spring-boot Enviroment Setting - https://www.lesstif.com/pages/viewpage.action?pageId=14090588
