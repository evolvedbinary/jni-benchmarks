#include <string>

namespace jnibench {

class FooObject {
  public:
    FooObject(const std::string& n, int64_t v) : name(n), value(v){}

    const std::string& GetName() const { return name; }
    int64_t GetValue() const { return value; }

  private:
    const std::string name;
    const int64_t value;
};

} // namespace jnibench