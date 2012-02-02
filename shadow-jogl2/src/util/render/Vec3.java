package util.render;

public class Vec3{
  public float x;
  public float y;
  public float z;
  
  public Vec3(float x, float y, float z) {// 全ての値を指定するコンストラクタです。
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vec3(float x, float y) {// Zの値が0になるコンストラクタです。
    this(x, y, 0);
  }

  public Vec3() {// 全ての値が0になるコンストラクタです。
    this(0, 0, 0);
  }

  public Vec3 Sub(Vec3 k) {// ベクトルを引いた結果を返します。
    return new Vec3(x - k.x, y - k.y, z - k.z);
  }

  public Vec3 Add(Vec3 k) {// ベクトルを足した結果を返します。
    return new Vec3(x + k.x, y + k.y, z + k.z);
  }

  public Vec3 Mul(float k) {// ベクトルをスカラ倍した結果を返します。
    return new Vec3(x * k, y * k, z * k);
  }

  public Vec3 Div(float k) {// ベクトルをスカラ量で割った結果を返します。
    if (k == 0)
      return this;
    return new Vec3(x / k, y / k, z / k);
  }

  public Vec3 cSub(Vec3 k) {// ベクトルを引きます。
    return new Vec3(x -= k.x, y -= k.y, z -= k.z);
  }

  public Vec3 cAdd(Vec3 k) {// ベクトルを足します。
    return new Vec3(x += k.x, y += k.y, z += k.z);
  }

  public Vec3 cMul(float k) {// ベクトルをスカラ倍します。
    return new Vec3(x *= k, y *= k, z *= k);
  }

  public Vec3 cDiv(float k) {// ベクトルをスカラ量で割ります。
    if (k == 0)
      return this;
    return new Vec3(x /= k, y /= k, z /= k);
  }

  public Vec3 Cro(Vec3 k) {// ベクトルの外積を返します。※2DではZのみ
    return new Vec3(y * k.z - z * k.y, z * k.x - x * k.z, x * k.y - y * k.x);
  }

  public float Dot(Vec3 k) {// ベクトルの内積を返します。
    return x * k.x + y * k.y + z * k.z;
  }

  public float Len() {// ベクトルの長さを返します。
    return (float) Math.sqrt(x * x + y * y + z * z);
  }

  public void Nor() {// ベクトルを正規化します。
    float len = Len();
    if (len == 0)
      return;
    else
      len = 1 / len;
    x *= len;
    y *= len;
    z *= len;
  }
}
