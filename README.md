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

> * AWS Setting
	* EC2 Port Setting
  : http://docs.aws.amazon.com/ko_kr/AWSEC2/latest/UserGuide/get-set-up-for-amazon-ec2.html
  * LB HTTPS Certification Setting
  : http://docs.aws.amazon.com/ko_kr/IAM/latest/UserGuide/id_credentials_server-certs_create.html
  * Server Timezone Setting
  : http://hotpotato.tistory.com/31

> * Docker Install in EC2 System
  > * Spring-boot Sample Test
  	: https://spring.io/guides/gs/spring-boot-docker/
  	>		### In docker shell - Use boot2docker
  		  ### in docker shell$ mvn package docker:build
  		  ### in docker shell$ docker login
  		  ### in docker shell$ docker push "Repository"
				
> * DNS Setting (Route 53)
	: http://wingsnote.com/57
	
> * Postfix Mail Server Setting
	: https://help.ubuntu.com/community/PostfixBasicSetupHowto
	: https://kldp.org/node/136332
	: http://fsteam.tistory.com/67
	
> * ETC
	: https://treewiki.s3.amazonaws.com/docker/springboot-aws-docker.html
	: https://brunch.co.kr/@brunchqvxt/1
	
### 3. Ref

>	- Docker repo - dotozambo/dotozambo
	- Git Menual - https://rogerdudler.github.io/git-guide/index.ko.html
