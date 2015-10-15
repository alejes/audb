package audb.table;

import audb.page.Page;
import audb.page.PageCache;
import audb.page.PageManager;
import audb.type.Type;
import audb.type.TypeUtil;

import java.nio.charset.StandardCharsets;


public class Table {

	private PageCache pageCache;

	private long INFO_PAGE = 0;
	private int TYPES_INFO = 20;
	private int INFO_SIZE = 32;

	private String[] names;
	private Type[] types;
	private int recordSize;
	private int maxRecords;



	public Table(String tablename) throws Exception {
		pageCache = new PageCache("db/" + tablename);
	}

	public void init() throws Exception {

		Page page = pageCache.getPage(INFO_PAGE);
		page.pin();
		int ptr = TYPES_INFO;
		int length = page.data[ptr++];
		this.names = new String[length];
		this.types = new Type[length];
		for(int i = 0; i < names.length; i++) {
			byte type = page.data[ptr++];
			int nameLength = page.data[ptr++];
			byte[] name = new byte[nameLength];
			for(int j = 0; j < name.length; j++)
				name[j] = page.data[ptr + j];
			ptr += name.length;
			this.names[i] = new String(name, StandardCharsets.UTF_8);
			this.types[i] = TypeUtil.makeType(type);
			System.out.println(names[i] + " " + types[i].getId()); //TODO
		}
		page.write();
		page.unpin();

		calculateRecordSize();
		System.out.println("recordSize = " + recordSize); //TODO
	}

	public void create(Type[] types, String[] names) throws Exception {

		if(types.length != names.length)
			throw new Exception("Table.java"); // TODO

		Page page = pageCache.getPage(INFO_PAGE);
		page.pin();
		int ptr = TYPES_INFO;
		page.data[ptr++] = (byte) types.length;
		for(int i = 0; i < types.length; i++) {
			page.data[ptr++] = types[i].getId();
			byte[] name = names[i].getBytes();
			page.data[ptr++] = (byte)name.length;
			for(int j = 0; j < name.length; j++)
				page.data[ptr + j] = name[j];
			ptr += name.length;
		}
		page.write();
		page.unpin();

		this.names = new String[names.length];
		this.types = new Type[types.length];
		System.arraycopy(names, 0, this.names, 0, names.length);
		System.arraycopy(types, 0, this.types, 0, types.length);
		calculateRecordSize();
	}

	public String[] getNames() {
		return names;
	}

	public Type[] getTypes() {
		return types;
	}

    public Object[] read(long pageNum, int offset) {
		Page page = pageCache.getPage(pageNum);
		page.pin();
		int ptr = offset * recordSize;
		Object[] objects = new Object[types.length];
		for(int i = 0; i < types.length; i++) {
			byte[] data = new byte[types[i].getSize()];
			System.arraycopy(page.data, ptr, data, 0, types[i].getSize());
			objects[i] = types[i].fromBytes(data);
			ptr += types[i].getSize();
		}
		page.unpin();
        return objects;
    }

    public void write(long pageNum, int offset, Object[] objects) throws Exception {
    	Page page = pageCache.getPage(pageNum);
		page.pin();
		int ptr = offset * recordSize;
		for(int i = 0; i < types.length; i++) {
			byte[] data = types[i].toBytes(objects[i]);
			System.arraycopy(data, 0, page.data, ptr, data.length);
			ptr += data.length;
		}
		ptr += 1;
		if(ptr != recordSize)
			throw new Exception("Table.java");
		page.write();
		page.unpin();
    }

    private void calculateRecordSize() {
    	recordSize = 0;
    	for(int i = 0; i < types.length; i++) {
    		recordSize += types[i].getSize();
    	}
    	recordSize += 1;
    	maxRecords = (PageManager.PAGE_SIZE - INFO_SIZE) / recordSize;
    }

    public void write(Object[] data) {
    	
    }

    public void close() {
    	pageCache.close();
    }

}
