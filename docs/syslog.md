# SYSLOG

Currently, the configuration for syslog appenders is defined in the `config/logback.xml` files within the `backend` and `pss` module.

It is defined as follows :

```$xslt
<appender name="SYSLOG" class="ch.qos.logback.classic.net.SyslogAppender">
    <!--Need to change this-->
    <syslogHost>127.0.0.1</syslogHost>
    <facility>USER</facility>
    <port>10514</port>
    <throwableExcluded>true</throwableExcluded>
    <suffixPattern>%msg</suffixPattern>
</appender>
```

As of now, OSCARS sends syslog events when OSCARS and PSS starts or stops as well as when a BUILD or DISMANTLE action starts or finishes.

For localhost testing, do the following

```$xslt
brew install rsyslog
brew services start rsyslog
mkdir /usr/local/var/run/

vi /usr/local/etc/rsyslog.conf
```

Contents of the `rsyslog.conf` file

```$xslt
#rsyslog.conf contents:
$ModLoad imudp
$UDPServerRun 10514

# Provides TCP syslog reception
$ModLoad imtcp
$InputTCPServerRun 10514

*.*                /usr/local/var/log/rsyslogd.log
```

Then, 

```$xslt
brew services restart rsyslog
/usr/local/var/log/rsyslogd.log
```

Once you restart oscars, the contents of the `rsyslogd.log` file will start to populate.