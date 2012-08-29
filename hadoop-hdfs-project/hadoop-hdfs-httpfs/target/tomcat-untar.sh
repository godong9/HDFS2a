which cygpath 2> /dev/null
                      if [ $? = 1 ]; then
                      BUILD_DIR="/home/godong/Project/HDFS2a/hadoop-hdfs-project/hadoop-hdfs-httpfs/target"
                      else
                      BUILD_DIR=`cygpath --unix '/home/godong/Project/HDFS2a/hadoop-hdfs-project/hadoop-hdfs-httpfs/target'`
                      fi
                      cd $BUILD_DIR/tomcat.exp
                      tar xzf /home/godong/Project/HDFS2a/hadoop-hdfs-project/hadoop-hdfs-httpfs/downloads/tomcat.tar.gz