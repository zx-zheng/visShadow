package util.render;

public class Vec3d{
  public double x;
  public double y;
  public double z;
  
  public Vec3d(double x, double y, double z) {// 全ての値を指定するコンストラクタです。
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vec3d(double x, double y) {// Zの値が0になるコンストラクタです。
    this(x, y, 0);
  }

  public Vec3d() {// 全ての値が0になるコンストラクタです。
    this(0, 0, 0);
  }

  public Vec3d Sub(Vec3d k) {// ベクトルを引いた結果を返します。
    return new Vec3d(x - k.x, y - k.y, z - k.z);
  }

  public Vec3d Add(Vec3d k) {// ベクトルを足した結果を返します。
    return new Vec3d(x + k.x, y + k.y, z + k.z);
  }

  public Vec3d Mul(double k) {// ベクトルをスカラ倍した結果を返します。
    return new Vec3d(x * k, y * k, z * k);
  }

  public Vec3d Div(double k) {// ベクトルをスカラ量で割った結果を返します。
    if (k == 0)
      return this;
    return new Vec3d(x / k, y / k, z / k);
  }

  public Vec3d cSub(Vec3d k) {// ベクトルを引きます。
    return new Vec3d(x -= k.x, y -= k.y, z -= k.z);
  }

  public Vec3d cAdd(Vec3d k) {// ベクトルを足します。
    return new Vec3d(x += k.x, y += k.y, z += k.z);
  }

  public Vec3d cMul(double k) {// ベクトルをスカラ倍します。
    return new Vec3d(x *= k, y *= k, z *= k);
  }

  public Vec3d cDiv(double k) {// ベクトルをスカラ量で割ります。
    if (k == 0)
      return this;
    return new Vec3d(x /= k, y /= k, z /= k);
  }

  public Vec3d Cro(Vec3d k) {// ベクトルの外積を返します。※2DではZのみ
    return new Vec3d(y * k.z - z * k.y, z * k.x - x * k.z, x * k.y - y * k.x);
  }

  public double Dot(Vec3d k) {// ベクトルの内積を返します。
    return x * k.x + y * k.y + z * k.z;
  }

  public double Len() {// ベクトルの長さを返します。
    return (double) Math.sqrt(x * x + y * y + z * z);
  }

  public void Nor() {// ベクトルを正規化します。
    double len = Len();
    if (len == 0)
      return;
    else
      len = 1 / len;
    x *= len;
    y *= len;
    z *= len;
  }
}
