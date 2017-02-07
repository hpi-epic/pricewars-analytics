FROM hseeberger/scala-sbt:latest

ENV APP_HOME /analytics
RUN mkdir $APP_HOME
WORKDIR $APP_HOME

ADD build.sbt $APP_HOME
ADD project/build.properties $APP_HOME/project/
ADD project/plugins.sbt $APP_HOME/project/

RUN sbt update

ADD . $APP_HOME

RUN sbt assembly
