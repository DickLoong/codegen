# -*- coding: utf-8 -*-

import datetime
from django.db import models
#from django.contrib import admin
import xadmin
from django.utils.encoding import force_unicode

# Create your models here.
class Server(models.Model):
    SERVER_STATUS = (
        ('C1','待处理'),
        ('C2','待分配'),
        ('C3','运营中'),
        ('C4','测试使用中'),
        ('C5','开发使用中'),
        ('C6','故障中'),
        ('C7','待下架'),
        ('C8','退役'),
    )
    server_code = models.CharField('云主机名', max_length=30,unique=True)   #机器序列号
    uuid = models.CharField('云主机uuid',max_length=64,unique=True)
    #tag_kingdee = models.CharField('金蝶标签',max_length=20,unique=True)
    ip_inner = models.IPAddressField('内网IP', blank=True,default='192.168.240.',unique=True,null=True)              #内网IP
    ip_vip = models.IPAddressField('VIP',blank=True,null=True)
    ip_outter = models.IPAddressField('外网IP',blank=True,unique=True,null=True)             #外网IP
    #ip_om = models.IPAddressField('管网IP',blank=True,default='192.168.225.',unique=True,null=True)                 #管理网IP
    status = models.CharField('机器状态',max_length=2,choices=SERVER_STATUS,default='C2')         #机器状态
    site = models.CharField('所在机房',max_length=50)          #所在机房
    #cabinets_code = models.CharField('机柜编码',max_length=20) #机柜编码
    #cabinets_pos = models.CharField('机柜位置', max_length=20)
    department = models.CharField('所属部门',max_length=30)    #所属部门
    #group = models.CharField('所属小组',max_length=30)         #所属小组
    product = models.CharField('所属产品',max_length=30)       #所属产品
    module = models.CharField('所属模块',max_length=200)
    time_store = models.DateField('入库时间',default=datetime.datetime.now(),blank=True, null=True)
    time_rack = models.DateField('上架时间',default=datetime.datetime.now(), blank=True, null=True)
    warranty = models.DateField('保修期', blank=True,null=True)
    operator = models.CharField('维护人员',max_length=20)
    backup_operator = models.CharField('备份人员',max_length=20)
    virtual = models.BooleanField('是否虚拟机',default=False,blank=True)
    virtual_host = models.CharField('所在母机', max_length=256,blank=True,null=True)
    hardware_type = models.CharField('机器型号',blank=True,max_length='100',default='UNKNOWN')
    hardware_manual = models.CharField('硬件说明',blank=True,max_length=300)
    #harddisk_raid = models.CharField('Raid',max_length=10)
    harddisk_size = models.IntegerField('硬盘大小(GB)')
    CPU_cores = models.IntegerField('CPU核数')
    #CPU_type = models.CharField('CPU型号',max_length=30,default="Intel")
    mem_size = models.IntegerField('内存大小(GB)')
    OS_version = models.CharField('OS说明',blank=True,max_length=100,default='Centos 6.6')
    level = models.CharField('服务级别',max_length=20,blank=True)
    remark = models.CharField('备注',max_length=300,null=True)
    hostname = models.CharField('主机名', max_length=128, null=True, blank=True)
    root_psw = models.CharField('root密码', max_length=256, null=True, blank=True)
    accounts = models.CharField('账号(多个账号以,分隔)', max_length=512, null=True, blank=True)
    def __unicode__(self):
        return u'%s/%s' % (self.uuid,self.ip_inner)

class ServerAdmin(object):
    list_display = ('server_code','ip_inner')


class Process(models.Model):
    PROCESS_STATUS = (
        ('P1','未分配'),
        ('P2','分配使用'),
        ('P3','正式使用'),
        ('P4','测试使用'),
        ('P5','待撤消'),
        ('P6','撤消中'),
        ('P7','已撤消'),
        ('P8','故障中'),
        ('P9','登记使用'),
    )

    PORT_TYPE= (
        ('tcp','tcp'),
        ('udp','udp'),
        ('other','其它(不关注)')
    )
    c_id = models.ForeignKey('Server', verbose_name='所在服务器')
    m_id = models.ForeignKey('Module', verbose_name='所属模块')
    name = models.CharField('进程名称', max_length=30,default='java')
    type = models.CharField('进程类型', max_length=30,default='jetty应用')
    keyword = models.CharField('关键字', max_length=30,default='java')
    path = models.CharField('存放路径', max_length=200,default='/kingdee/jetty/domains/')
    start_script = models.CharField('启动脚本', max_length=200,default='$main_path$/bin/start.sh')
    stop_script = models.CharField('停止脚本', max_length=200,default='$main_path$/bin/stop.sh')
    status = models.CharField('进程状态', max_length=2,choices=PROCESS_STATUS,default='P1')
    main_port = models.IntegerField('主端口', default=8080)
    main_port_type = models.CharField('主端口类型',choices=PORT_TYPE,default='other',max_length=5)
    jmx_port = models.IntegerField('JMX端口',default=8090, null=True, blank=True)
    other_port = models.IntegerField('其它端口',default=0, null=True, blank=True)
    container_version = models.CharField('进程容器版本',max_length=30,default='0', null=True, blank=True)
    code_version = models.CharField('代码版本',max_length=30,default='0', null=True, blank=True)
    update_time = models.DateTimeField('变更时间',default=datetime.datetime.now(), null=True, blank=True)

    class Meta:
        ordering = ['status','c_id']
        unique_together = ('c_id','main_port')

    def __unicode__(self):
        return u'%s:%s' % (self.c_id.ip_inner,self.main_port)

class ProcessAdmin(object):
    list_display = ('c_id','m_id','name')
    list_select_related = ('c_id','m_id')


class Module(models.Model):
    name = models.CharField('模块名称', max_length=100)
    product = models.ForeignKey('Product', related_name='product_id', verbose_name='所属产品')
    short_name = models.CharField('模块简称', max_length=64, blank=True, null=True)
    type = models.CharField('模块类型', max_length=20, blank=True, null=True)
    dis = models.CharField('模块简介', max_length=300, blank=True, null=True)
    mark = models.CharField('模块备注', max_length=300, blank=True, null=True)
    git = models.CharField('git库路径', max_length=200, blank=True, null=True)
    url = models.CharField('代码url', max_length=100)
    code_path = models.CharField('代码路径', max_length=100)
    warm_up_url = models.CharField('预热url', max_length=100, blank=True, null=True)
    update_time = models.DateTimeField('更新时间', blank=True, null=True)
    f_script = models.CharField('前置脚本', max_length=300,blank=True, null=True)
    b_script = models.CharField('后置脚本', max_length=300,blank=True, null=True)
    path = models.CharField('部署路径', max_length=200)
    domain_name = models.CharField('域名', max_length=200, blank=True, null=True)
    def __unicode__(self):
        return force_unicode(self.product.name+"/"+self.name)

class ModuleAdmin(object):
    list_display = ('name', 'product', 'dis')



class Product(models.Model):
    product_set = models.ForeignKey('ProductSet')
    name = models.CharField('产品名称',max_length=200)
    manager = models.CharField('负责人',max_length=100,default='ethanlin')
    def __unicode__(self):
        return u'%s' % self.name

class ProductAdmin(object):
    list_display = ('name', 'manager')

class ProductSet(models.Model):
    name = models.CharField('产品集名称',max_length=100,default='云之家')
    manager = models.CharField('负责人',max_length=100,default='ethanlin')
    def __unicode__(self):
        return u'%s' % self.name

class ProductSetAdmin(object):
    list_display = ('name', 'manager')


class Event(models.Model):
    title = models.CharField('事件标题', max_length=256)
    start_time = models.DateTimeField("开始时间")
    end_time = models.DateTimeField("结束时间", default=datetime.datetime.now())
    responsible_user = models.CharField("责任人", max_length=256)
    other_user = models.CharField("相关人", max_length=256, blank=True, null=True)
    description = models.TextField("事件描述", max_length=1024)
    influence = models.TextField("事件影响", max_length=1024)
    reason = models.TextField("事件原因", max_length=1024)
    improvement = models.TextField("改进措施",max_length=1024)
    def __unicode__(self):
        return u"%s" % self.title

class EventAdmin(object):
    list_display = ('id', 'title')




