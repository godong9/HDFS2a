run() {
                        echo "\$ ${@}"
                        "${@}"
                        res=$?
                        if [ $res != 0 ]; then
                          echo
                          echo "Failed!"
                          echo
                          exit $res
                        fi
                      }

                      ROOT=`cd /home/godong/Project/HDFS2a/hadoop-dist/..;pwd`
                      echo
                      echo "Current directory `pwd`"
                      echo
                      run rm -rf hadoop-2.0.1-SNAPSHOT
                      run mkdir hadoop-2.0.1-SNAPSHOT
                      run cd hadoop-2.0.1-SNAPSHOT
                      run cp -r $ROOT/hadoop-common-project/hadoop-common/target/hadoop-common-2.0.1-SNAPSHOT/* .
                      run cp -r $ROOT/hadoop-hdfs-project/hadoop-hdfs/target/hadoop-hdfs-2.0.1-SNAPSHOT/* .
                      run cp -r $ROOT/hadoop-hdfs-project/hadoop-hdfs-httpfs/target/hadoop-hdfs-httpfs-2.0.1-SNAPSHOT/* .
                      run cp -r $ROOT/hadoop-yarn-project/target/hadoop-yarn-project-2.0.1-SNAPSHOT/* .
                      run cp -r $ROOT/hadoop-mapreduce-project/target/hadoop-mapreduce-2.0.1-SNAPSHOT/* .
                      run cp -r $ROOT/hadoop-tools/hadoop-tools-dist/target/hadoop-tools-dist-2.0.1-SNAPSHOT/* .
                      echo
                      echo "Hadoop dist layout available at: /home/godong/Project/HDFS2a/hadoop-dist/target/hadoop-2.0.1-SNAPSHOT"
                      echo