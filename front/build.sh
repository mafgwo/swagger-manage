#!/bin/sh

# 服务端文件夹名称
server_folder_name="swagger-manage-server"
server_dest="../$server_folder_name/src/main/resources/public"

rm -rf dist/*
npm run build:prod
echo "复制dist文件内容到$server_dest"
rm -rf $server_dest
mkdir -p $server_dest
cp -r dist/* $server_dest
