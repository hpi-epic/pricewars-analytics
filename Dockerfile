FROM hseeberger/scala-sbt:latest

ENV APP_HOME /analytics
RUN mkdir $APP_HOME
WORKDIR $APP_HOME

ADD . $APP_HOME

RUN sbt update
RUN sbt assembly
