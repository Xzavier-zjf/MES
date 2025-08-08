[33mcommit 624ed0aaa0d5e92d597449aafd55aabf1bbe3c82[m[33m ([m[1;36mHEAD[m[33m -> [m[1;32mmaster[m[33m)[m
Merge: 484207e a19a02a
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Sun Jul 6 22:52:41 2025 +0800

    Merge remote-tracking branch

[33mcommit 484207eae5b033489a0d528034d40fea3499d80e[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Sun Jul 6 22:01:00 2025 +0800

    完善了其他页面数据不能实时互通显示及其他优化体验的功能

[33mcommit 1a983878d9852bba75dac6204d6415825b1237c8[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Sun Jul 6 19:52:56 2025 +0800

    规范了其他页面api设置

[33mcommit f1d5b1a61ba93c41850198fd13f21102a933a88b[m
Author: 风起青萍 <2114086570@qq.com>
Date:   Sun Jul 6 08:07:27 2025 +0000

    修改设备利用率计算逻辑，将比例转换为百分比
    
    Signed-off-by: 风起青萍 <2114086570@qq.com>

[33mcommit a19a02a27cb24d0639ba604666bd7fc52a709b30[m[33m ([m[1;31mgithub/master[m[33m)[m
Merge: ff2f227 caea17b
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Fri Jul 4 17:16:10 2025 +0800

    Merge branch 'master' of https://github.com/Xzavier-zjf/Lime

[33mcommit ee37934dd1d7e88833f19b5574746831b88fff7d[m
Merge: 6e95abb d6b7381
Author: Mo <mowenjian1014@163.com>
Date:   Fri Jul 4 16:25:11 2025 +0800

    Merge remote-tracking branch 'origin/master'

[33mcommit 6e95abb5e4c14a7bce98cf92122f8672cdceb84d[m
Author: Mo <mowenjian1014@163.com>
Date:   Fri Jul 4 16:24:55 2025 +0800

    增加了用户服务和服务修改

[33mcommit 758500d2656f02eb55723180690f3a5019d56819[m
Author: Mo <mowenjian1014@163.com>
Date:   Fri Jul 4 16:17:33 2025 +0800

    设备运行状态

[33mcommit caea17b3ecad8d7b8a90ba7ae7e5376485d0a572[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Fri Jul 4 15:27:58 2025 +0800

    Delete src/view directory

[33mcommit 9be403ac47f12399b54b3b47b648486f40090bcc[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Fri Jul 4 15:26:53 2025 +0800

    Update Home.vue--修复了首页面的“计划下发趋势”图的鼠标停留不能显示的问题

[33mcommit 3162766b1936802a428ab7167e3484268de97f6b[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Fri Jul 4 15:13:22 2025 +0800

    Revert "修复了首页面的“计划下发趋势”图的鼠标停留不能显示的问题"
    
    This reverts commit e28fab277367b6485f10a0937b9c2eb5b44aa1bc.

[33mcommit d6b73819d083a30d41b8a69b570ba80a37ad91c2[m
Author: 风起青萍 <2114086570@qq.com>
Date:   Fri Jul 4 06:36:10 2025 +0000

    update mes-frontend/src/view/Home.vue-修复了首页面的“计划下发趋势”图的鼠标停留不能显示的问题
    
    Signed-off-by: 风起青萍 <2114086570@qq.com>

[33mcommit 1d67450c6af1242bb72e815cc7779b4e352b6a79[m
Author: tacular <2534431843@qq.com>
Date:   Fri Jul 4 02:50:15 2025 +0000

    update readme.txt.

[33mcommit e28fab277367b6485f10a0937b9c2eb5b44aa1bc[m
Author: zjf <2114086570@qq.com>
Date:   Thu Jul 3 22:17:22 2025 +0800

    修复了首页面的“计划下发趋势”图的鼠标停留不能显示的问题

[33mcommit 1a683b3afb76790d2fccd94ca152c7bcce6bd04d[m
Author: zjf <2114086570@qq.com>
Date:   Thu Jul 3 22:03:23 2025 +0800

    修复了首页面的“计划下发趋势”图鼠标停留不能显示数据的问题

[33mcommit ff2f2272b97c2bf3cd8489ac3a5dafe0fd99ff00[m
Author: zjf <2114086570@qq.com>
Date:   Thu Jul 3 21:43:17 2025 +0800

    修复了首页面的“计划下发趋势”图在鼠标停留下不能显示的问题

[33mcommit edfa6f17a0c50c44f4c465757f5d5105c4b77e78[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Thu Jul 3 19:55:06 2025 +0800

    Create auth_service_db.sql
    
    登录数据库

[33mcommit cc07194cc7c85e61f12ef92b70a48b99cde1c1a8[m
Merge: 1eec206 950238b
Author: tacular <2534431843@qq.com>
Date:   Wed Jul 2 17:49:02 2025 +0800

    Merge branch 'master' of https://gitee.com/qsgg666/shoujike-mes

[33mcommit 1eec206d93318e5c9e3257c6ceb5054eaa39c18b[m
Author: tacular <2534431843@qq.com>
Date:   Wed Jul 2 17:48:57 2025 +0800

    修改了设备、工艺的数据库

[33mcommit 950238ba6b0e98a4771153fe471433c245191bcd[m
Author: 风起青萍 <2114086570@qq.com>
Date:   Wed Jul 2 06:52:03 2025 +0000

    前端说明
    
    Signed-off-by: 风起青萍 <2114086570@qq.com>

[33mcommit a63dd62c8f4a39cb2b99700eb744c7d702299694[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Wed Jul 2 14:50:12 2025 +0800

    Add files via upload

[33mcommit f75458534ab72067df0ef36fb65194ad8fc8cb5d[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Wed Jul 2 14:49:11 2025 +0800

    Add files via upload

[33mcommit cd652294547b7419fa8d3861618312a4b00d6ec9[m
Author: tacular <2534431843@qq.com>
Date:   Wed Jul 2 14:42:26 2025 +0800

    首页实现

[33mcommit 3dd665c572e3a1ecbd24575972746ab9bb3a2877[m
Merge: f919891 42ab79e
Author: tacular <2534431843@qq.com>
Date:   Wed Jul 2 14:38:58 2025 +0800

    Merge branch 'master' of https://gitee.com/qsgg666/shoujike-mes
    Merge remote changes to master

[33mcommit f9198918a13f21a67c77ccd8a4b60d80061b6463[m
Author: tacular <2534431843@qq.com>
Date:   Wed Jul 2 14:15:54 2025 +0800

    首页实现

[33mcommit 8933b3156b95a140d6c975343b8503020fae87a9[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Wed Jul 2 13:58:05 2025 +0800

    项目介绍
    
    项目介绍：
    
    项目名称：MES系统
    项目描述：MES系统是一个基于Spring Boot的微服务架构系统，旨在实现注塑/印刷生产线的数字化管理。系统包括生产管理、设备管理、工艺流程管理等多个模块，旨在提高生产效率、降低成本和提升产品质量。

[33mcommit 549b436c822f014142768eebb2fd3a179ef954cd[m
Author: Xzavier-zjf <z2114086570@163.com>
Date:   Wed Jul 2 13:56:31 2025 +0800

    Update readme.txt

[33mcommit 42ab79e5ac3327ea0555f876d053d525d5dcc7bc[m
Author: 风起青萍 <2114086570@qq.com>
Date:   Wed Jul 2 05:53:40 2025 +0000

    update readme.txt.
    
    Signed-off-by: 风起青萍 <2114086570@qq.com>

[33mcommit ffd4a875e26f6d86fbb1b91b8f96b068128d7391[m
Author: 风起青萍 <2114086570@qq.com>
Date:   Wed Jul 2 05:52:08 2025 +0000

    update readme.txt.
    
    Signed-off-by: 风起青萍 <2114086570@qq.com>

[33mcommit 3c0c5f18cf841e40b65dd52f50f86129b72f1024[m
Author: 风起青萍 <2114086570@qq.com>
Date:   Wed Jul 2 05:51:34 2025 +0000

    update readme.txt.
    
    Signed-off-by: 风起青萍 <2114086570@qq.com>

[33mcommit 4f8399005a4d48560aba78b1d9fede0aa497d1e7[m
Author: 风起青萍 <2114086570@qq.com>
Date:   Wed Jul 2 05:50:04 2025 +0000

    update readme.txt.
    
    Signed-off-by: 风起青萍 <2114086570@qq.com>

[33mcommit 8f4256ec701becf914c9c8c4649569517bb700cf[m
Author: 风起青萍 <2114086570@qq.com>
Date:   Wed Jul 2 05:47:56 2025 +0000

    项目介绍
    项目介绍：
    
    项目名称：MES系统
    项目描述：MES系统是一个基于Spring Boot的微服务架构系统，旨在实现注塑/印刷生产线的数字化管理。系统包括生产管理、设备管理、工艺流程管理等多个模块，旨在提高生产效率、降低成本和提升产品质量。
    
    Signed-off-by: 风起青萍 <2114086570@qq.com>

[33mcommit 9af6f4c123830777aeeade85d8c208f40635e23b[m
Author: zjf <2114086570@qq.com>
Date:   Wed Jul 2 13:37:50 2025 +0800

    项目介绍：
    
    项目名称：MES系统
    项目描述：MES系统是一个基于Spring Boot的微服务架构系统，旨在实现注塑/印刷生产线的数字化管理。系统包括生产管理、设备管理、工艺流程管理等多个模块，旨在提高生产效率、降低成本和提升产品质量。

[33mcommit 72161d372807c6f252d7f8ede5d5265db03c7f2b[m
Merge: 65ae6fa 5473c31
Author: Mo <mowenjian1014@163.com>
Date:   Wed Jul 2 12:06:53 2025 +0800

    Merge branch 'master' of https://gitee.com/qsgg666/shoujike-mes
    
    # Conflicts:
    #       MES-demo/services/auth-service/pom.xml
    #       MES-demo/services/auth-service/src/main/resources/application.yml
    #       mes-frontend/package-lock.json

[33mcommit 5473c319b5f60a69631a5ad61854a917e03a4b7b[m
Author: tacular <2534431843@qq.com>
Date:   Wed Jul 2 11:14:04 2025 +0800

    优化了业务

[33mcommit 65ae6fad7da65efbfed9b8ce6f8fd6c93bed742e[m
Author: Mo <mowenjian1014@163.com>
Date:   Mon Jun 30 17:49:43 2025 +0800

    .

[33mcommit ed80df2e4cc59ed08f1c13e22f99f9deea342f65[m
Author: Mo <mowenjian1014@163.com>
Date:   Mon Jun 30 17:12:37 2025 +0800

    QIANDUAN

[33mcommit 5001dd579c723110e5ad19dde03e25789684878e[m
Merge: 5b5eea7 7fc6679
Author: Mo <mowenjian1014@163.com>
Date:   Mon Jun 30 16:15:39 2025 +0800

    Merge remote-tracking branch 'origin/master'
    
    # Conflicts:
    #       mes-frontend/src/components/TaskDialog.vue

[33mcommit 5b5eea769fb50e11e9cf05fbc976d78d3f222704[m
Author: Mo <mowenjian1014@163.com>
Date:   Mon Jun 30 16:12:40 2025 +0800

    增加了用户服务和服务修改

[33mcommit 7fc6679c44101e32d8a248e12a91f385e6a06bb2[m
Author: tacular <2534431843@qq.com>
Date:   Sat Jun 28 23:13:17 2025 +0800

    更新了数据库

[33mcommit 998c08936c4679ef853157ba9bc2f80a7dc543ab[m
Author: tacular <2534431843@qq.com>
Date:   Sat Jun 28 22:20:52 2025 +0800

    接了设备、注塑、图案的代码

[33mcommit 334b7163b2c457d8e459ac47abc0389b6abe2f3a[m
Author: Mo <mowenjian1014@163.com>
Date:   Sat Jun 28 22:00:17 2025 +0800

    前端任务·

[33mcommit f2c2670277b420d4438f28b19370ba094ce37701[m
Author: Mo <mowenjian1014@163.com>
Date:   Sat Jun 28 13:10:15 2025 +0800

    生产计划的前后端和任务管理前后端

[33mcommit 2c51e78242a9fa77e288caeef09ef60017f0581f[m
Author: Mo <mowenjian1014@163.com>
Date:   Thu Jun 26 19:36:13 2025 +0800

    接了生产计划接口

[33mcommit 4499d6ed009f8d2eb5f6bc85078cbe0529d5414b[m
Author: Mo <mowenjian1014@163.com>
Date:   Thu Jun 26 15:36:50 2025 +0800

    前端代码

[33mcommit d6d7df77fbb66e1c506180a30eca11a9b997b164[m
Merge: 7fdb233 ec8224b
Author: tacular <2534431843@qq.com>
Date:   Wed Jun 25 22:33:18 2025 +0800

    Merge remote-tracking branch 'origin/master'

[33mcommit 7fdb233a70e023bd7a18a2de688b23213ff0ab88[m
Author: Mo <mowenjian1014@163.com>
Date:   Wed Jun 25 19:03:24 2025 +0800

    修改了微服务架构

[33mcommit ec8224bfdf7f659ebeafbc050301b0dea3a560b0[m
Author: Mo <mowenjian1014@163.com>
Date:   Wed Jun 25 19:03:24 2025 +0800

    创建任务的控制层修改，服务层修改，以及FeignClient 调用设备服务

[33mcommit 15978024b5d58df4249b07ac48412389be0027cd[m
Author: Mo <mowenjian1014@163.com>
Date:   Wed Jun 25 16:34:18 2025 +0800

    改

[33mcommit 936fdf54ea8f5fa6a5f861b802681e6bbfbc0349[m
Author: tacular <2534431843@qq.com>
Date:   Fri Jun 20 16:12:40 2025 +0800

    第一次提交后端代码

[33mcommit 23198fbec2e2d1a50628e2cadc26bf5fd015f3ff[m
Author: tacular <2534431843@qq.com>
Date:   Thu Jun 19 22:17:43 2025 +0800

    提交了项目后端框架
