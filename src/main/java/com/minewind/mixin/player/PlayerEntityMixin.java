package com.minewind;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ModelSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelSystem.class);

    private final Map<String, CustomModel> customModels;
    private final Map<Class<? extends Entity>, String> entityModelMappings;

    public ModelSystem() {
        this.customModels = new HashMap<>();
        this.entityModelMappings = new HashMap<>();
    }

    public void initialize() {
        LOGGER.info("Initializing custom model system...");
        registerDefaultModels();
        LOGGER.info("Custom model system initialized with {} models", customModels.size());
    }

    public void onClientStart() {
        LOGGER.info("Model system started");
    }

    public void onClientStop() {
        LOGGER.info("Model system stopped");
        customModels.clear();
        entityModelMappings.clear();
    }

    private void registerDefaultModels() {
        registerModel("npc/nerevarine", new CustomModel("nerevarine", ModelType.OBJ));
        registerModel("creature/guar", new CustomModel("guar", ModelType.OBJ));
        registerModel("creature/cliff_racer", new CustomModel("cliff_racer", ModelType.OBJ));
        registerModel("weapon/sword", new CustomModel("sword", ModelType.JSON));
        registerModel("weapon/dagger", new CustomModel("dagger", ModelType.JSON));
        registerModel("armor/helmet", new CustomModel("helmet", ModelType.JSON));
    }

    public void registerModel(String id, CustomModel model) {
        customModels.put(id, model);
        LOGGER.debug("Registered model: {}", id);
    }

    public void mapEntityToModel(Class<? extends Entity> entityClass, String modelId) {
        entityModelMappings.put(entityClass, modelId);
        LOGGER.debug("Mapped entity {} to model {}", entityClass.getSimpleName(), modelId);
    }

    public CustomModel getModelForEntity(Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();
        String modelId = entityModelMappings.get(entityClass);

        if (modelId != null && customModels.containsKey(modelId)) {
            return customModels.get(modelId);
        }

        for (Map.Entry<Class<? extends Entity>, String> entry : entityModelMappings.entrySet()) {
            if (entry.getKey().isAssignableFrom(entityClass)) {
                return customModels.get(entry.getValue());
            }
        }

        return null;
    }

    public CustomModel getModel(String id) {
        return customModels.get(id);
    }

    public void renderModel(CustomModel model, Entity entity, MatrixStack matrices,
                            VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (model != null && model.isLoaded()) {
            model.render(entity, matrices, vertexConsumers, light, overlay);
        }
    }

    public void loadModel(String id, String path, ModelType type) {
        CustomModel model = new CustomModel(id, type);
        model.loadFromFile(path);
        customModels.put(id, model);
        LOGGER.info("Loaded model {} from {}", id, path);
    }

    public CustomModel createViewModel(String id, String modelPath) {
        CustomModel viewModel = new CustomModel(id + "_view", ModelType.OBJ);
        viewModel.setIsViewModel(true);
        viewModel.loadFromFile(modelPath);
        customModels.put(id + "_view", viewModel);
        LOGGER.info("Created viewmodel: {}", id);
        return viewModel;
    }

    public enum ModelType {
        OBJ, JSON, BLOCKBENCH
    }

    public static class CustomModel {
        private final String id;
        private final ModelType type;
        private boolean loaded;
        private boolean isViewModel;
        private EntityModel<?> model;
        private ModelPart root;

        public CustomModel(String id, ModelType type) {
            this.id = id;
            this.type = type;
            this.loaded = false;
            this.isViewModel = false;
        }

        public void loadFromFile(String path) {
            this.loaded = true;
            LOGGER.debug("Loaded model {} from file: {}", id, path);
        }

        public void render(Entity entity, MatrixStack matrices,
                           VertexConsumerProvider vertexConsumers, int light, int overlay) {
            if (loaded && model != null) {
                model.render(matrices, vertexConsumers, light, overlay);
            }
        }

        public boolean isLoaded() {
            return loaded;
        }

        public String getId() {
            return id;
        }

        public ModelType getType() {
            return type;
        }

        public void setIsViewModel(boolean isViewModel) {
            this.isViewModel = isViewModel;
        }

        public boolean isViewModel() {
            return isViewModel;
        }

        public void setModel(EntityModel<?> model) {
            this.model = model;
        }

        public void setRootPart(ModelPart root) {
            this.root = root;
        }
    }
}
