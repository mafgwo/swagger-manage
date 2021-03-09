#!/bin/sh

# 服务端文件夹名称
server_folder_name="swagger-manage-server"
# 执行文件名称
app_name="swagger-manage"
# 构建目录
dist_dir="dist"
# 输出目录
target_dir="$dist_dir/$app_name"

echo "开始构建..."

# 先执行前端构建
cd front
sh build.sh

cd ..

mvn clean package

echo "复制文件到$target_dir"

rm -rf $dist_dir
mkdir -p $target_dir

cp -r $server_folder_name/target/*.jar $target_dir/$app_name.jar
cp -r script/* $target_dir

echo "打成zip包"

cd $dist_dir
zip -r -q "$app_name.zip" $app_name

echo "构建完毕"
