FROM hseeberger/scala-sbt:latest

ENV APP_HOME /analytics
RUN mkdir $APP_HOME
WORKDIR $APP_HOME

ADD build.sbt $APP_HOME
ADD project/build.properties $APP_HOME/project/
ADD project/plugins.sbt $APP_HOME/project/

ADD utils/ $APP_HOME/utils

RUN sbt update

ADD . $APP_HOME

RUN mkdir -p target/jars
RUN mv -f start-jobmanager.sh target/jars/start-jobmanager.sh

RUN sbt assembly
