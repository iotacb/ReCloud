package de.kostari.cloud.core.utils.tween;

public enum Ease implements EaseFunction {
    LINEAR {
        @Override
        public float apply(float t) {
            return t;
        }
    },
    IN_SINE {
        @Override
        public float apply(float t) {
            return 1.0f - (float) Math.cos((t * Math.PI) * 0.5f);
        }
    },
    OUT_SINE {
        @Override
        public float apply(float t) {
            return (float) Math.sin((t * Math.PI) * 0.5f);
        }
    },
    IN_OUT_SINE {
        @Override
        public float apply(float t) {
            return -((float) Math.cos(Math.PI * t) - 1.0f) * 0.5f;
        }
    },
    IN_QUAD {
        @Override
        public float apply(float t) {
            return t * t;
        }
    },
    OUT_QUAD {
        @Override
        public float apply(float t) {
            return 1.0f - (1.0f - t) * (1.0f - t);
        }
    },
    IN_OUT_QUAD {
        @Override
        public float apply(float t) {
            return t < 0.5f ? 2.0f * t * t : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 2.0f) * 0.5f;
        }
    },
    IN_CUBIC {
        @Override
        public float apply(float t) {
            return t * t * t;
        }
    },
    OUT_CUBIC {
        @Override
        public float apply(float t) {
            return 1.0f - (float) Math.pow(1.0f - t, 3.0f);
        }
    },
    IN_OUT_CUBIC {
        @Override
        public float apply(float t) {
            return t < 0.5f ? 4.0f * t * t * t : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 3.0f) * 0.5f;
        }
    },
    IN_BACK {
        @Override
        public float apply(float t) {
            float c1 = 1.70158f;
            float c3 = c1 + 1.0f;
            return c3 * t * t * t - c1 * t * t;
        }
    },
    OUT_BACK {
        @Override
        public float apply(float t) {
            float c1 = 1.70158f;
            float c3 = c1 + 1.0f;
            return 1.0f + c3 * (float) Math.pow(t - 1.0f, 3.0f) + c1 * (float) Math.pow(t - 1.0f, 2.0f);
        }
    },
    IN_OUT_BACK {
        @Override
        public float apply(float t) {
            float c1 = 1.70158f;
            float c2 = c1 * 1.525f;
            if (t < 0.5f) {
                return ((float) Math.pow(2.0f * t, 2.0f) * ((c2 + 1.0f) * 2.0f * t - c2)) * 0.5f;
            }
            return ((float) Math.pow(2.0f * t - 2.0f, 2.0f)
                    * ((c2 + 1.0f) * (t * 2.0f - 2.0f) + c2) + 2.0f) * 0.5f;
        }
    },
    OUT_BOUNCE {
        @Override
        public float apply(float t) {
            float n1 = 7.5625f;
            float d1 = 2.75f;
            if (t < 1.0f / d1) {
                return n1 * t * t;
            }
            if (t < 2.0f / d1) {
                float shifted = t - 1.5f / d1;
                return n1 * shifted * shifted + 0.75f;
            }
            if (t < 2.5f / d1) {
                float shifted = t - 2.25f / d1;
                return n1 * shifted * shifted + 0.9375f;
            }
            float shifted = t - 2.625f / d1;
            return n1 * shifted * shifted + 0.984375f;
        }
    };
}
