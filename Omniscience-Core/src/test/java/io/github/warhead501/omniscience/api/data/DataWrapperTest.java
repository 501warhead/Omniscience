package io.github.warhead501.omniscience.api.data;

public class DataWrapperTest {
/*
    @Mock
    World world;

    @Mock
    ItemStack itemStack;

    @Mock
    Entity entity;

    @Spy
    BlockState state;

    @Mock
    BlockData data;

    private UUID worldId = UUID.randomUUID();

    @Before
    public void setUp() throws Exception {
        //BlockState state = spy(BlockState.class);
        when(state.getX()).thenReturn(1);
        when(state.getY()).thenReturn(1);
        when(state.getZ()).thenReturn(1);
        when(state.getWorld()).thenReturn(world);
        when(world.getUID()).thenReturn(worldId);
        when(state.getType()).thenReturn(Material.STONE);
        when(state.getBlockData()).thenReturn(data);
        when(data.getAsString()).thenReturn("serializedData");
        when(entity.getType()).thenReturn(EntityType.COW);
        when(entity.getWorld()).thenReturn(world);
        when(itemStack.serialize()).thenReturn(getItemstackAsMap());
        when(itemStack.getType()).thenReturn(Material.STICK);
    }

    @Test
    public void testOf() {
    }

    @Test
    public void get() {
        DataWrapper wrapper = setupDataWrapper();
        Optional<String> oString = wrapper.getString(DataKey.of("STRING_TEST"));
        Optional<Integer> oInt = wrapper.getInt(DataKey.of("INT_TEST"));
        Optional<ArrayList<Long>> oArray = wrapper.get(DataKey.of("LIST_TEST"));
        assertTrue(oString.isPresent());
        assertTrue(oInt.isPresent());
        assertTrue(oArray.isPresent());
        assertEquals("test", oString.get());
        assertEquals(Integer.valueOf(123), oInt.get());
    }

    @Test
    public void getKeys() {
        DataWrapper wrapper = setupDataWrapper();
        DataKey testKey = DataKey.of("TEST_KEY");
        wrapper.set(testKey, "test");
        assertTrue(wrapper.getKeys().contains(testKey));
    }

    private DataWrapper setupDataWrapper() {
        DataWrapper wrapper = DataWrapper.createNew();
        wrapper.set(DataKey.of("STRING_TEST"), "test");
        wrapper.set(DataKey.of("INT_TEST"), 123);
        wrapper.set(DataKey.of("LIST_TEST"), new ArrayList<Long>());
        wrapper.set(DataKey.of("LOCATION_TEST"), DataWrapper.of(setupLocation()));
        wrapper.set(DataKey.of("ENTITY_TEST"), DataWrapper.of(entity));
        //wrapper.set(DataKey.of("BLOCK_TEST"), DataWrapper.of(state));
        wrapper.set(DataKey.of("ITEM_TEST"), DataWrapper.of(itemStack));
        return wrapper;
    }

    private Location setupLocation() {
        return new Location(world, 1, 1, 1);
    }

    private Map<String, Object> getItemstackAsMap() {
        Map<String, Object> localmap = new HashMap<>();
        localmap.put("material", Material.STICK);
        localmap.put("quantity", 1);
        return localmap;
    }
    */
}