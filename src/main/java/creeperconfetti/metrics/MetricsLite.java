package creeperconfetti.metrics;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;


import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.UUID;

public class MetricsLite {
  public static final int B_STATS_VERSION = 1;
  private static final String URL = "https://bStats.org/submitData/bukkit";
  private boolean enabled;
  private static boolean logFailedRequests;
  private static boolean logSentData;
  private static boolean logResponseStatusText;
  private static String serverUUID;
  private final Plugin plugin;
  private final int pluginId;
  
  static {
    if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
      
      String defaultPackage = new String(new byte[] { 111, 114, 103, 46, 98, 115, 116, 97, 116, 115, 46, 98, 117, 107, 107, 105, 116 });
      
      String examplePackage = new String(new byte[] { 121, 111, 117, 114, 46, 112, 97, 99, 107, 97, 103, 101 });
      
      if (MetricsLite.class.getPackage().getName().equals(defaultPackage) || MetricsLite.class.getPackage().getName().equals(examplePackage)) {
        throw new IllegalStateException("bStats 指标类没有正确地重新定位！");
      }
    } 
  }

  public MetricsLite(Plugin plugin, int pluginId) {
    if (plugin == null) {
      throw new IllegalArgumentException("插件不能为null！");
    }
    this.plugin = plugin;
    this.pluginId = pluginId;

    
    File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
    File configFile = new File(bStatsFolder, "config.yml");
    YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    
    if (!config.isSet("serverUuid")) {

      
      config.addDefault("enabled", Boolean.valueOf(true));
      
      config.addDefault("serverUuid", UUID.randomUUID().toString());
      
      config.addDefault("logFailedRequests", Boolean.valueOf(false));
      
      config.addDefault("logSentData", Boolean.valueOf(false));
      
      config.addDefault("logResponseStatusText", Boolean.valueOf(false));

      
      config.options().header("bStats 收集一些数据给插件作者，例如有多少服务器在使用他们的插件。\n为了尊重他们的劳动成果，你不应该禁用它。\n这对服务器性能几乎没有影响！\n访问 https://bStats.org/ 了解更多信息：")
        .copyDefaults(true);
      try {
        config.save(configFile);
      } catch (IOException iOException) {}
    } 

    
    serverUUID = config.getString("serverUuid");
    logFailedRequests = config.getBoolean("logFailedRequests", false);
    this.enabled = config.getBoolean("enabled", true);
    logSentData = config.getBoolean("logSentData", false);
    logResponseStatusText = config.getBoolean("logResponseStatusText", false);
    if (this.enabled) {
      boolean found = false;
      
      for (Class<?> service : (Iterable<Class<?>>)Bukkit.getServicesManager().getKnownServices()) {
        try {
          service.getField("B_STATS_VERSION");
          found = true;
          break;
        } catch (NoSuchFieldException noSuchFieldException) {}
      } 
      
      Bukkit.getServicesManager().register(MetricsLite.class, this, plugin, ServicePriority.Normal);
      if (!found)
      {
        startSubmitting();
      }
    } 
  }

  
  public boolean isEnabled() {
    return this.enabled;
  }

  
  private void startSubmitting() {
    final Timer timer = new Timer(true);
    timer.scheduleAtFixedRate(new TimerTask()
        {
          public void run() {
            if (!MetricsLite.this.plugin.isEnabled()) {
              timer.cancel();
              
              return;
            } 
            
            Bukkit.getScheduler().runTask(MetricsLite.this.plugin, () -> MetricsLite.this.submitData());
          }
        }, 300000L, 1800000L);
  }

  
  public JsonObject getPluginData() {
    JsonObject data = new JsonObject();
    
    String pluginName = this.plugin.getDescription().getName();
    String pluginVersion = this.plugin.getDescription().getVersion();
    
    data.addProperty("pluginName", pluginName);
    data.addProperty("id", Integer.valueOf(this.pluginId));
    data.addProperty("pluginVersion", pluginVersion);
    data.add("customCharts", (JsonElement)new JsonArray());
    
    return data;
  }

  
  private JsonObject getServerData() {
    int playerAmount;
    try {
      Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers", new Class[0]);

      
      playerAmount = onlinePlayersMethod.getReturnType().equals(Collection.class) ? ((Collection)onlinePlayersMethod.invoke(Bukkit.getServer(), new Object[0])).size() : ((Player[])onlinePlayersMethod.invoke(Bukkit.getServer(), new Object[0])).length;
    } catch (Exception e) {
      playerAmount = Bukkit.getOnlinePlayers().size();
    } 
    int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
    String bukkitVersion = Bukkit.getVersion();
    String bukkitName = Bukkit.getName();

    
    String javaVersion = System.getProperty("java.version");
    String osName = System.getProperty("os.name");
    String osArch = System.getProperty("os.arch");
    String osVersion = System.getProperty("os.version");
    int coreCount = Runtime.getRuntime().availableProcessors();
    
    JsonObject data = new JsonObject();
    
    data.addProperty("serverUUID", serverUUID);
    
    data.addProperty("playerAmount", Integer.valueOf(playerAmount));
    data.addProperty("onlineMode", Integer.valueOf(onlineMode));
    data.addProperty("bukkitVersion", bukkitVersion);
    data.addProperty("bukkitName", bukkitName);
    
    data.addProperty("javaVersion", javaVersion);
    data.addProperty("osName", osName);
    data.addProperty("osArch", osArch);
    data.addProperty("osVersion", osVersion);
    data.addProperty("coreCount", Integer.valueOf(coreCount));
    
    return data;
  }

  
  private void submitData() {
    JsonObject data = getServerData();
    
    JsonArray pluginData = new JsonArray();
    
    for (Class<?> service : (Iterable<Class<?>>)Bukkit.getServicesManager().getKnownServices()) {
      try {
        service.getField("B_STATS_VERSION");

        for (RegisteredServiceProvider<?> provider : (Iterable<RegisteredServiceProvider<?>>)Bukkit.getServicesManager().getRegistrations(service)) {
          try {
            Object plugin = provider.getService().getMethod("getPluginData", new Class[0]).invoke(provider.getProvider(), new Object[0]);
            if (plugin instanceof JsonObject) {
              pluginData.add((JsonElement)plugin); continue;
            }
            try {
              Class<?> jsonObjectJsonSimple = Class.forName("org.json.simple.JSONObject");
              if (plugin.getClass().isAssignableFrom(jsonObjectJsonSimple)) {
                Method jsonStringGetter = jsonObjectJsonSimple.getDeclaredMethod("toJSONString", new Class[0]);
                jsonStringGetter.setAccessible(true);
                String jsonString = (String)jsonStringGetter.invoke(plugin, new Object[0]);
                JsonObject object = (new JsonParser()).parse(jsonString).getAsJsonObject();
                pluginData.add((JsonElement)object);
              }
            } catch (ClassNotFoundException e) {

              if (logFailedRequests) {
                this.plugin.getLogger().log(Level.SEVERE, "遇到意外异常", e);
              }
            }

          } catch (NullPointerException|NoSuchMethodException|IllegalAccessException|java.lang.reflect.InvocationTargetException nullPointerException) {}
        }

      } catch (NoSuchFieldException noSuchFieldException) {}
    }

    data.add("plugins", (JsonElement)pluginData);

    
    (new Thread(() -> {
          
          try {
            sendData(this.plugin, data);
          } catch (Exception e) {
            
            if (logFailedRequests) {
              this.plugin.getLogger().log(Level.WARNING, "无法提交插件统计信息：" + this.plugin.getName(), e);
            }
          } 
        })).start();
  }

  
  private static void sendData(Plugin plugin, JsonObject data) throws Exception {
    if (data == null) {
      throw new IllegalArgumentException("数据不能为空！");
    }
    if (Bukkit.isPrimaryThread()) {
      throw new IllegalAccessException("此方法不得在主线程上调用！");
    }
    if (logSentData) {
      plugin.getLogger().info("正在发送数据到 bStats：" + data);
    }

    HttpsURLConnection connection = (HttpsURLConnection)(new URL(URL)).openConnection();



    byte[] compressedData = compress(data.toString());

    
    connection.setRequestMethod("POST");
    connection.addRequestProperty("Accept", "application/json");
    connection.addRequestProperty("Connection", "close");
    connection.addRequestProperty("Content-Encoding", "gzip");
    connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("User-Agent", "MC-Server/1");

    
    connection.setDoOutput(true);
    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream()); 
    try { outputStream.write(compressedData);
      outputStream.close(); } catch (Throwable throwable) { try { outputStream.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }
     throw throwable; }
     StringBuilder builder = new StringBuilder();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    try { String line;
      while ((line = bufferedReader.readLine()) != null) {
        builder.append(line);
      }
      bufferedReader.close(); } catch (Throwable throwable) { try { bufferedReader.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }
     throw throwable; }
     if (logResponseStatusText) {
      plugin.getLogger().info("已发送数据到 bStats 并收到响应：" + builder);
    }
  }

  
  private static byte[] compress(String str) throws IOException {
    if (str == null) {
      return null;
    }
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(outputStream); 
    try { gzip.write(str.getBytes(StandardCharsets.UTF_8));
      gzip.close(); } catch (Throwable throwable) { try { gzip.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }
     return outputStream.toByteArray();
  }
}
