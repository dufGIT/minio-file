package com.file.resource.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author df
 * @Description:
 * @Date 2023/11/28 17:14
 */
@Slf4j
@Component
public class RedisDao {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 删除hash表中的值
     *
     * @param key 键 不能为null
     */
    public void hdel(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * 根据大key和小Key获取hash里的value
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public Object hGet(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 根据大key获取map
     *
     * @param key 键 不能为null
     * @return true 存在 false不存在
     */
    public Map<String, Object> hGet(String key) {
        return redisTemplate.opsForHash().entries(key);
    }


    /**
     * 根据大key获取map中的values
     *
     * @param key 键 不能为null
     * @return true 存在 false不存在
     */
    public List<String> hGetValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }


    /**
     * 判断hash表中是否有该项的值
     *
     * @param key 键 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 根据key获取hash键值对
     *
     * @param key 键 不能为null
     * @return Map<Object, Object> 存在 false不存在
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }


    //------------------------------------------------------------------------list处理------------------------------------------------------------------

    /**
     * 在列表的最左边塞入一个value
     *
     * @param key
     * @param value
     */
    public void lpush(String key, String value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 在列表的最右边塞入一个value
     *
     * @param key
     * @param value
     */
    public void rpush(String key, String value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 获取指定索引位置的值, index为-1时，表示返回的是最后一个；当index大于实际的列表长度时，返回null
     *
     * @param key
     * @param index
     * @return
     */
    public Object indexValue(String key, int index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 获取范围值，闭区间，start和end这两个下标的值都会返回; end为-1时，表示获取的是最后一个；
     * <p>
     * 如果希望返回最后两个元素，可以传入  -2, -1
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> range(String key, int start, int end) {
        return redisTemplate.opsForList().range(key, start, end);
    }


    /**
     * 设置list中指定下标的值，采用干的是替换规则, 最左边的下标为0；-1表示最右边的一个
     *
     * @param key
     * @param index
     * @param value
     */
    public void set(String key, Integer index, String value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 删除列表中值为value的元素，总共删除count次；
     * <p>
     * 如原来列表为 【1， 2， 3， 4， 5， 2， 1， 2， 5】
     * 传入参数 value=2, count=1 表示删除一个列表中value为2的元素
     * 则执行后，列表为 【1， 3， 4， 5， 2， 1， 2， 5】
     *
     * @param key
     * @param value
     * @param count
     */
    public Boolean remove(String key, String value, int count) {
        try {
            redisTemplate.opsForList().remove(key, count, value);
            return true;
        } catch (Exception e) {
            log.error("删除列表的元素值失败！|参数,key:{},value:{},count:{}", key, value, count);
            return false;
        }
    }

    /**
     * 判断redis中是否有该key
     *
     * @param key 键 不能为null
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
