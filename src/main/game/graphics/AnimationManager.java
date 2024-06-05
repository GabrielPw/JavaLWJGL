package main.game.graphics;

public class AnimationManager {

    private float spriteSize;
    private int actualAnimation; // Linha atual no atlas
    private int actualFrame; // Coluna atual no atlas
    private int quantityFrames;
    private float framesPerSecond;
    private float frameTime; // Tempo por frame (1.0 / frames por segundo)
    private float elapsedTime;
    public AnimationManager (int quantityFrames, int actualAnimation, float spriteSize, float framesPerSecond){

        this.actualAnimation = actualAnimation;
        this.quantityFrames = quantityFrames;
        this.spriteSize = spriteSize;
        this.actualFrame = 0;
        this.frameTime = 1.0f / framesPerSecond;
        this.elapsedTime = 0.0f;
    }

    public void play(float deltaTime){

        elapsedTime += deltaTime;
        if (elapsedTime >= frameTime) {
            actualFrame = (actualFrame + 1) % quantityFrames;
            elapsedTime -= frameTime;
        }
    }

    public int getActualFrame() {
        return actualFrame;
    }

    public int getActualAnimation() {
        return actualAnimation;
    }

    public void setActualAnimation(int actualAnimation, int quantityFrames) {
        this.actualAnimation = actualAnimation;
        /*
        this.quantityFrames = quantityFrames;
        this.actualFrame = 0;
        this.elapsedTime = 0.0f;
        */
    }

    public void setQuantityFrames(int quantityFrames) {
        this.quantityFrames = quantityFrames;
    }

    public void setFramesPerSecond(float framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
    }
}
