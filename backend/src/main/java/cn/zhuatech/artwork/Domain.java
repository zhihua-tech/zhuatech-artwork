/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.artwork;
import org.springframework.stereotype.Component;
import java.util.*;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import static cn.zhuatech.artwork.Model.*;
import static cn.zhuatech.artwork.Engine.*;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component public class Domain {
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static Map<String,Object> copy(Row r){return new LinkedHashMap<>(r.data());}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal n(Row r,String k){return num(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal z(Map<String,Object>d,String k){return d.containsKey(k)?num(d,k):BigDecimal.ZERO;}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static String t(Row r,String k){return txt(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static List<Row> linked(Engine e,User u,String module,String key,String id){return e.all(u,module).stream().filter(r->t(r,key).equals(id)).toList();}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void unique(Engine e,User u,String module,Map<String,Object>d,String key){require(e.all(u,module).stream().noneMatch(r->t(r,key).equalsIgnoreCase(txt(d,key))),"重复的"+key);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void dates(Map<String,Object>d,String from,String to){require(!date(d,to).isBefore(date(d,from)),"结束日期不能早于开始日期");}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void change(Engine e,User u,Row row,String state,Map<String,Object>d,String note){e.save(u,row,state,d,"LINKED",note);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void edit(Engine e,User u,Row r,Map<String,Object>d){
  if(r.module().equals("readings")){require(e.ref(u,r.data(),"job","jobs").state().equals("RUNNING")&&txt(d,"job").equals(t(r,"job")),"仅进行中的任务可以修改测量值，且不得迁移任务");require(linked(e,u,"readings","job",t(r,"job")).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"point").equals(txt(d,"point"))),"测量点编号重复");return;}
  if(r.module().equals("versions")){require(e.ref(u,r.data(),"artwork","artworks").state().equals("DRAFT"),"已送审稿件不可修改");require(txt(d,"artwork").equals(t(r,"artwork"))&&num(d,"revision").compareTo(n(r,"revision"))==0,"版本不能迁移任务或改写版本号");return;}
  for(var m:e.spec().modules())for(Row other:e.all(u,m.key()))if(!other.id().equals(r.id())&&other.data().values().stream().anyMatch(v->r.id().equals(v)))throw new Failure(409,"资料已有下游引用，请新建版本而不是改写历史");
  var fields=e.spec().module(r.module()).fields().stream().map(Field::key).toList();
  r.data().forEach((k,v)->{if(!fields.contains(k))d.put(k,v);});
  if(d.containsKey("start")&&d.containsKey("end"))dates(d,"start","end");
  if(d.containsKey("from")&&d.containsKey("to"))dates(d,"from","to");
  for(String key:List.of("serial","sku","invoice","invoiceNo","lockNo"))if(d.containsKey(key))require(e.all(u,r.module()).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,key).equalsIgnoreCase(txt(d,key))),"重复唯一业务标识: "+key);
  if(d.containsKey("bonusRate"))require(num(d,"bonusRate").compareTo(num(d,"baseRate"))>=0,"达档返利率不能低于基础返利率");
  if(d.containsKey("lifeLimit"))require(num(d,"serviceEvery").compareTo(num(d,"lifeLimit"))<=0,"保养间隔不能大于寿命");
  if(d.containsKey("defects"))require(num(d,"defects").compareTo(num(d,"shots"))<=0,"不良数不能超过生产次数");
  if(d.containsKey("nps"))require(num(d,"nps").compareTo(BigDecimal.TEN)<=0&&num(d,"csat").compareTo(new BigDecimal("5"))<=0,"评价分数超出范围");
  if(d.containsKey("oxygenMin"))require(num(d,"oxygenMin").compareTo(num(d,"oxygenMax"))<0,"氧气下限须小于上限");
  if(r.module().equals("invoices"))require(e.all(u,"invoices").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"shipment").equals(txt(d,"shipment"))),"运单已关联结算账单");
  if(r.module().equals("sales")){Row program=e.ref(u,d,"program","programs");require(program.state().equals("ACTIVE")&&!date(d,"soldAt").isBefore(date(program.data(),"start"))&&!date(d,"soldAt").isAfter(date(program.data(),"end")),"协议状态或销售日期无效");}
  if(r.module().equals("jobs")){Row instrument=e.ref(u,d,"instrument","instruments"),standard=e.ref(u,d,"standard","standards");require(!instrument.state().equals("RETIRED")&&t(instrument,"unit").equals(t(standard,"unit")),"器具状态或计量单位无效");require(!date(d,"performedAt").isAfter(LocalDate.now()),"不能记录未来校准");}
  if(r.module().equals("permits")){require(ChronoUnit.DAYS.between(date(d,"start"),date(d,"end"))<=7,"许可最长七天");require(t(e.ref(u,d,"isolation","isolations"),"location").equals(txt(d,"location")),"隔离区域不匹配");}
  if(r.module().equals("responses")){require(e.all(u,"responses").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"survey").equals(txt(d,"survey"))&&t(x,"customer").equals(txt(d,"customer"))),"客户已存在该问卷反馈");require(t(e.ref(u,d,"customer","customers"),"consent").equals("YES"),"客户未允许反馈邀请");}
  if(r.module().equals("products")){String barcode=txt(d,"barcode");require(barcode.matches("\\d{13}"),"条码须为 EAN-13");int sum=0;for(int x=0;x<12;x++)sum+=(barcode.charAt(x)-'0')*(x%2==0?1:3);require((10-sum%10)%10==barcode.charAt(12)-'0',"EAN-13 校验位不正确");}

 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Map<String,Object> metrics(Engine e,User u){
  var out=new LinkedHashMap<String,Object>();out.put("待发布任务",e.all(u,"artworks").stream().filter(r->r.state().equals("REVIEW")).count());out.put("待审签项",e.all(u,"reviews").stream().filter(r->r.state().equals("PENDING")).count());out.put("有效发布稿",e.all(u,"artworks").stream().filter(r->r.state().equals("RELEASED")).count());;return out;
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void create(Engine e,User u,String module,Map<String,Object>d){switch(module){case "products" -> {unique(e,u,module,d,"sku");require(txt(d,"barcode").matches("\\d{13}"),"条码须为 EAN-13");String barcode=txt(d,"barcode");int sum=0;for(int x=0;x<12;x++)sum+=(barcode.charAt(x)-'0')*(x%2==0?1:3);require((10-sum%10)%10==barcode.charAt(12)-'0',"EAN-13 校验位不正确");}
case "versions" -> {Row artwork=e.ref(u,d,"artwork","artworks");require(artwork.state().equals("DRAFT"),"仅草稿任务可创建版本");int max=linked(e,u,"versions","artwork",artwork.id()).stream().mapToInt(v->n(v,"revision").intValueExact()).max().orElse(0);require(num(d,"revision").intValueExact()==max+1,"版本号必须连续递增");} default -> {} }}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public String action(Engine e,User u,Row r,String action,Map<String,Object>i,Map<String,Object>d){
  String k=r.module()+"."+action;switch(k){
case "artworks.submit" -> {
 Row version=e.ref(u,i,"version","versions");require(t(version,"artwork").equals(r.id()),"版本不属于当前稿件任务");require(version.state().equals("DRAFT"),"版本已锁定");Row product=e.ref(u,d,"product","products");
 require(t(version,"barcode").equals(t(product,"barcode")),"包装条码与产品档案不一致");require(t(version,"copyText").contains(t(product,"requiredText")),"包装缺少必备文案");
 require(e.jdbc().queryForObject("SELECT COUNT(*) FROM attachment WHERE tenant=? AND record_id=?",Integer.class,u.tenant(),version.id())>0,"请先上传实际设计稿件");
 d.put("version",version.id());change(e,u,version,"REVIEW",copy(version),"送审冻结版本");
 for(String discipline:List.of("QUALITY","LEGAL","MARKETING"))e.ledger(u,"reviews","PENDING",Map.of("artwork",r.id(),"version",version.id(),"discipline",discipline));
}
case "artworks.reopen" -> {
 require(r.creator().equals(u.id())||u.role().equals("ADMIN"),"仅任务创建者或管理员可撤回");
 Row version=e.ref(u,d,"version","versions");change(e,u,version,"SUPERSEDED",copy(version),"撤回版本留痕，需新建修订版");
 for(Row review:linked(e,u,"reviews","artwork",r.id()))if(t(review,"version").equals(version.id()))change(e,u,review,"VOID",copy(review),"撤回审签");
 d.remove("version");
}
case "reviews.approve","reviews.reject" -> {
 Row artwork=e.ref(u,d,"artwork","artworks");require(artwork.state().equals("REVIEW")&&t(artwork,"version").equals(txt(d,"version")),"任务不在该版本审签阶段");
 Row version=e.ref(u,d,"version","versions");require(!version.creator().equals(u.id())&&!artwork.creator().equals(u.id()),"设计作者及任务创建者不可自审");
 require(linked(e,u,"reviews","artwork",artwork.id()).stream().noneMatch(v->t(v,"version").equals(version.id())&&t(v,"reviewerId").equals(u.id())),"同一人不能兼任本版本多个审核职能");
 d.putAll(i);d.put("reviewerId",u.id());d.put("reviewerName",u.username());
}
case "artworks.release" -> {
 Row version=e.ref(u,d,"version","versions");var reviews=linked(e,u,"reviews","artwork",r.id()).stream().filter(x->t(x,"version").equals(version.id())).toList();
 require(reviews.size()==3&&reviews.stream().allMatch(x->x.state().equals("APPROVED")),"质量、法务、市场三项审签尚未全部通过");
 require(reviews.stream().map(x->t(x,"reviewerId")).distinct().count()==3,"三项审签须由不同人员完成");
 var files=e.jdbc().queryForList("SELECT filename,digest,size_bytes FROM attachment WHERE tenant=? AND record_id=?",u.tenant(),version.id());require(!files.isEmpty(),"缺少发布文件");
 String manifest=e.encode(Map.of("files",files,"reviews",reviews.stream().map(Row::data).toList(),"version",version.data(),"publisher",u.username()));
 e.ledger(u,"releases","RELEASED",Map.of("artwork",r.id(),"version",version.id(),"manifest",manifest));change(e,u,version,"RELEASED",copy(version),"审批发布");d.put("publishedBy",u.username());
}
case "artworks.obsolete" -> {d.putAll(i);for(Row release:linked(e,u,"releases","artwork",r.id()))change(e,u,release,"OBSOLETE",copy(release),"稿件废止");Row version=e.ref(u,d,"version","versions");change(e,u,version,"OBSOLETE",copy(version),"稿件废止");}
 default -> {} }return null;
 }
}
