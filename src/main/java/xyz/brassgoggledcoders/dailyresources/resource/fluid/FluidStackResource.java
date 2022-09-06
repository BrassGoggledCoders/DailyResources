package xyz.brassgoggledcoders.dailyresources.resource.fluid;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.Choice;
import xyz.brassgoggledcoders.dailyresources.resource.Resource;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FluidStackResource implements Resource<FluidStack> {
    public static final Codec<FluidStackResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(TagKey.hashedCodec(Registry.FLUID_REGISTRY), ForgeRegistries.FLUIDS.getCodec())
                    .fieldOf("fluid")
                    .forGetter(FluidStackResource::getFluid),
            Codec.INT.optionalFieldOf("amount", FluidAttributes.BUCKET_VOLUME)
                    .forGetter(FluidStackResource::getAmount),
            CompoundTag.CODEC.optionalFieldOf("nbt")
                    .forGetter(fluidStackResource -> Optional.ofNullable(fluidStackResource.getNbt()))
    ).apply(instance, (item, count, nbt) -> new FluidStackResource(item, count, nbt.orElse(null))));

    @SuppressWarnings({"unchecked"})
    public static final Supplier<Codec<Choice<FluidStack>>> CHOICE_CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(instance -> instance.group(
                    Resource.RESOURCE_CODEC.get().fieldOf("resource").forGetter(Choice::getResource),
                    FluidStack.CODEC.fieldOf("object").forGetter(Choice::getObject)
            ).apply(instance, (resource, object) -> new Choice<>((Resource<FluidStack>) resource, object)))
    );

    private final Either<TagKey<Fluid>, Fluid> fluid;
    private final int amount;
    private final CompoundTag nbt;

    private final Supplier<Collection<Choice<FluidStack>>> choices = Suppliers.memoize(this::generateChoices);

    public FluidStackResource(Either<TagKey<Fluid>, Fluid> fluid, int amount, CompoundTag nbt) {
        this.fluid = fluid;
        this.amount = amount;
        this.nbt = nbt;
    }

    @Override
    @NotNull
    public ResourceType<FluidStack> getResourceType() {
        return DailyResourcesResources.FLUIDSTACK.get();
    }

    @Override
    @NotNull
    public Collection<Choice<FluidStack>> asChoices() {
        return this.choices.get();
    }

    @Override
    public boolean contains(FluidStack object) {
        return false;
    }

    @Override
    public <U> Optional<Resource<U>> cast(ResourceType<U> resourceType) {
        return Optional.empty();
    }

    @Override
    public Codec<Choice<FluidStack>> getChoiceCodec() {
        return CHOICE_CODEC.get();
    }

    public Either<TagKey<Fluid>, Fluid> getFluid() {
        return fluid;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public int getAmount() {
        return amount;
    }

    private List<Choice<FluidStack>> generateChoices() {
        return this.getFluid()
                .map(
                        tag -> Objects.requireNonNull(ForgeRegistries.FLUIDS.tags())
                                .getTag(tag)
                                .stream(),
                        Stream::of
                ).map(value -> new FluidStack(value, this.getAmount(), this.getNbt()))
                .map(fluidStack -> new Choice<>(this, fluidStack))
                .toList();
    }
}
